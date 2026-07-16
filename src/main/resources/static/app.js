document.addEventListener('DOMContentLoaded', () => {
    // Instâncias dos gráficos do Chart.js (para podermos destruí-los ao recarregar)
    let chartRankingUf = null;
    let chartSalaryTrends = null;
    let chartHiringByRegion = null;

    // Elementos da Interface
    const topUfEl = document.getElementById('top-uf-value');
    const topSalaryEl = document.getElementById('top-salary-value');
    const totalAdmissionsEl = document.getElementById('total-admissions-value');
    
    const etlStatusPulse = document.getElementById('etl-status-pulse');
    const btnTriggerEtl = document.getElementById('btn-trigger-etl');
    const etlSpinner = document.getElementById('etl-spinner');
    
    const etlStatStatus = document.getElementById('etl-stat-status');
    const etlStatDate = document.getElementById('etl-stat-date');
    const etlStatRead = document.getElementById('etl-stat-read');
    const etlStatProcessed = document.getElementById('etl-stat-processed');
    const etlStatRejected = document.getElementById('etl-stat-rejected');
    
    const headerPeriodBadge = document.getElementById('header-period-badge');

    // Filtros
    const selectUf = document.getElementById('select-uf');
    const selectCbo = document.getElementById('select-cbo');
    const selectSeniority = document.getElementById('select-seniority');

    // Controle de Tema
    const btnThemeToggle = document.getElementById('btn-theme-toggle');
    const themeIcon = document.getElementById('theme-icon');

    // Inicialização do Tema
    const savedTheme = localStorage.getItem('theme') || 'dark-theme';
    document.body.className = savedTheme;
    themeIcon.textContent = savedTheme === 'dark-theme' ? '🌙' : '☀️';

    // Inicialização do Dashboard
    initFilters().then(() => {
        loadDashboardData();
    });
    loadEtlStatus();
    loadTemporalRange();

    // Event Listener do Botão do ETL
    btnTriggerEtl.addEventListener('click', () => {
        triggerEtl();
    });

    // Event Listeners para os Filtros (Reativos)
    selectUf.addEventListener('change', () => {
        loadDashboardData();
    });

    selectCbo.addEventListener('change', () => {
        loadDashboardData();
    });

    selectSeniority.addEventListener('change', () => {
        loadDashboardData();
    });

    // Event Listener para o Alternador de Tema
    btnThemeToggle.addEventListener('click', () => {
        const body = document.body;
        if (body.classList.contains('dark-theme')) {
            body.classList.replace('dark-theme', 'light-theme');
            themeIcon.textContent = '☀️';
            localStorage.setItem('theme', 'light-theme');
        } else {
            body.classList.replace('light-theme', 'dark-theme');
            themeIcon.textContent = '🌙';
            localStorage.setItem('theme', 'dark-theme');
        }
        // Recarregar os gráficos para redesenhar com as cores do novo tema
        loadDashboardData();
    });

    // Helper para extrair as cores dinâmicas baseadas no tema ativo
    function getThemeColors() {
        const isLight = document.body.classList.contains('light-theme');
        return {
            grid: isLight ? 'rgba(15, 23, 42, 0.06)' : 'rgba(255, 255, 255, 0.05)',
            text: isLight ? '#475569' : '#94a3b8',
            legend: isLight ? '#0f172a' : '#f8fafc',
            barRankBg: isLight ? 'rgba(79, 70, 229, 0.7)' : 'rgba(99, 102, 241, 0.75)',
            barRankBorder: isLight ? '#4f46e5' : '#6366f1',
            barSalaryBg: isLight ? 'rgba(5, 150, 105, 0.7)' : 'rgba(16, 185, 129, 0.75)',
            barSalaryBorder: isLight ? '#059669' : '#10b981',
            
            // Cores para Senioridades
            barJuniorBg: 'rgba(245, 158, 11, 0.75)',
            barJuniorBorder: '#f59e0b',
            barPlenoBg: isLight ? 'rgba(79, 70, 229, 0.75)' : 'rgba(99, 102, 241, 0.75)',
            barPlenoBorder: isLight ? '#4f46e5' : '#6366f1',
            barSeniorBg: 'rgba(16, 185, 129, 0.75)',
            barSeniorBorder: '#10b981'
        };
    }

    // Função para carregar as opções dos dropdowns de filtros dinamicamente
    async function initFilters() {
        try {
            const [resUfs, resCbos] = await Promise.all([
                fetch('/analytics/ufs'),
                fetch('/analytics/cbos')
            ]);

            const ufs = await resUfs.json();
            const cbos = await resCbos.json();

            // Povoar Dropdown de UFs
            ufs.forEach(uf => {
                const opt = document.createElement('option');
                opt.value = uf;
                opt.textContent = uf;
                selectUf.appendChild(opt);
            });

            // Povoar Dropdown de Cargos
            cbos.forEach(cbo => {
                const opt = document.createElement('option');
                opt.value = cbo;
                opt.textContent = cbo;
                selectCbo.appendChild(opt);
            });

        } catch (error) {
            console.error('Erro ao carregar listas de filtros:', error);
        }
    }

    // Busca o período de datas no banco de dados e atualiza o badge do cabeçalho
    async function loadTemporalRange() {
        try {
            const res = await fetch('/analytics/temporal-range');
            if (res.status === 204 || res.status === 404) {
                headerPeriodBadge.textContent = 'Sem dados no banco';
                return;
            }
            const data = await res.json();
            if (data.minCompetencia && data.maxCompetencia) {
                // Força timezone UTC para evitar oscilação de fuso horário
                const minDate = new Date(data.minCompetencia + 'T00:00:00');
                const maxDate = new Date(data.maxCompetencia + 'T00:00:00');
                
                const formatMonthYearFull = (date) => {
                    const months = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
                    return `${months[date.getMonth()]}/${date.getFullYear()}`;
                };
                
                headerPeriodBadge.textContent = `Período: ${formatMonthYearFull(minDate)} a ${formatMonthYearFull(maxDate)}`;
            } else {
                headerPeriodBadge.textContent = 'Dados Reais Novo CAGED';
            }
        } catch (error) {
            console.error('Erro ao buscar período temporal:', error);
            headerPeriodBadge.textContent = 'Erro ao carregar período';
        }
    }

    // Função Principal para Carregar os Dados das APIs Analíticas
    async function loadDashboardData() {
        const uf = selectUf.value;
        const cbo = selectCbo.value;

        try {
            const [resRanking, resSalaries, resRegions] = await Promise.all([
                fetch(`/analytics/ranking-uf?cbo=${cbo}`),
                fetch(`/analytics/salary-trends?uf=${uf}`),
                fetch(`/analytics/hiring-by-region?uf=${uf}&cbo=${cbo}`)
            ]);

            const rankingData = await resRanking.json();
            const salaryData = await resSalaries.json();
            const regionData = await resRegions.json();

            // Renderizar Gráficos e Métricas
            renderMetrics(rankingData, salaryData);
            renderRankingChart(rankingData);
            renderSalaryChart(salaryData);
            renderRegionChart(regionData, uf);

        } catch (error) {
            console.error('Erro ao carregar dados do dashboard:', error);
        }
    }

    // Função para Atualizar os Cards de Métricas Rápidas
    function renderMetrics(rankingData, salaryData) {
        // 1. UF Líder em TI
        if (rankingData && rankingData.length > 0) {
            topUfEl.textContent = `${rankingData[0].uf} (${rankingData[0].totalAdmissoesTech.toLocaleString('pt-BR')} vagas)`;
        } else {
            topUfEl.textContent = 'Sem dados';
        }

        // 2. Maior Média Salarial de TI
        if (salaryData && salaryData.length > 0) {
            const sortedSalaries = [...salaryData].sort((a, b) => b.salarioMedio - a.salarioMedio);
            const topRole = sortedSalaries[0];
            topSalaryEl.textContent = `R$ ${topRole.salarioMedio.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} (${topRole.ocupacao})`;
        } else {
            topSalaryEl.textContent = 'Sem dados';
        }

        // 3. Total de Admissões em TI
        if (rankingData && rankingData.length > 0) {
            const total = rankingData.reduce((sum, item) => sum + item.totalAdmissoesTech, 0);
            totalAdmissionsEl.textContent = total.toLocaleString('pt-BR');
        } else {
            totalAdmissionsEl.textContent = '0';
        }
    }

    // Gráfico 1: Ranking de UFs (Barra Horizontal)
    function renderRankingChart(data) {
        const ctx = document.getElementById('chart-ranking-uf').getContext('2d');
        const theme = getThemeColors();
        
        if (chartRankingUf) {
            chartRankingUf.destroy();
        }

        const labels = data.map(item => item.uf);
        const values = data.map(item => item.totalAdmissoesTech);

        chartRankingUf = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Admissões TI',
                    data: values,
                    backgroundColor: theme.barRankBg,
                    borderColor: theme.barRankBorder,
                    borderWidth: 1.5,
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { backgroundColor: '#1e293b', titleColor: '#fff', bodyColor: '#cbd5e1' }
                },
                scales: {
                    x: { grid: { color: theme.grid }, ticks: { color: theme.text } },
                    y: { grid: { display: false }, ticks: { color: theme.text } }
                }
            }
        });
    }

    // Gráfico 2: Média Salarial por Cargo (Suporta Agrupamento por Senioridade)
    function renderSalaryChart(data) {
        const ctx = document.getElementById('chart-salary-trends').getContext('2d');
        const theme = getThemeColors();
        const level = selectSeniority.value;

        if (chartSalaryTrends) {
            chartSalaryTrends.destroy();
        }

        const sortedData = data
            .filter(item => item.totalRegistros > 0)
            .sort((a, b) => b.salarioMedio - a.salarioMedio)
            .slice(0, 6); // Limita em 6 cargos para não embolar caso mostre todos

        const labels = sortedData.map(item => item.ocupacao);
        
        let datasets = [];
        let showLegend = false;

        if (level === 'ALL') {
            showLegend = true;
            datasets = [
                {
                    label: 'Júnior',
                    data: sortedData.map(item => item.salarioJunior),
                    backgroundColor: theme.barJuniorBg,
                    borderColor: theme.barJuniorBorder,
                    borderWidth: 1.5,
                    borderRadius: 4
                },
                {
                    label: 'Pleno',
                    data: sortedData.map(item => item.salarioPleno),
                    backgroundColor: theme.barPlenoBg,
                    borderColor: theme.barPlenoBorder,
                    borderWidth: 1.5,
                    borderRadius: 4
                },
                {
                    label: 'Sênior',
                    data: sortedData.map(item => item.salarioSenior),
                    backgroundColor: theme.barSeniorBg,
                    borderColor: theme.barSeniorBorder,
                    borderWidth: 1.5,
                    borderRadius: 4
                }
            ];
        } else if (level === 'MEDIA') {
            datasets = [{
                label: 'Média Geral (R$)',
                data: sortedData.map(item => item.salarioMedio),
                backgroundColor: theme.barPlenoBg,
                borderColor: theme.barPlenoBorder,
                borderWidth: 1.5,
                borderRadius: 6
            }];
        } else if (level === 'JUNIOR') {
            datasets = [{
                label: 'Salário Júnior (R$)',
                data: sortedData.map(item => item.salarioJunior),
                backgroundColor: theme.barJuniorBg,
                borderColor: theme.barJuniorBorder,
                borderWidth: 1.5,
                borderRadius: 6
            }];
        } else if (level === 'PLENO') {
            datasets = [{
                label: 'Salário Pleno (R$)',
                data: sortedData.map(item => item.salarioPleno),
                backgroundColor: theme.barPlenoBg,
                borderColor: theme.barPlenoBorder,
                borderWidth: 1.5,
                borderRadius: 6
            }];
        } else if (level === 'SENIOR') {
            datasets = [{
                label: 'Salário Sênior (R$)',
                data: sortedData.map(item => item.salarioSenior),
                backgroundColor: theme.barSeniorBg,
                borderColor: theme.barSeniorBorder,
                borderWidth: 1.5,
                borderRadius: 6
            }];
        }

        chartSalaryTrends = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { 
                        display: showLegend,
                        labels: { color: theme.text, font: { family: 'Inter', weight: '500' } }
                    },
                    tooltip: { backgroundColor: '#1e293b', titleColor: '#fff', bodyColor: '#cbd5e1' }
                },
                scales: {
                    x: { 
                        grid: { display: false }, 
                        ticks: { 
                            color: theme.text,
                            callback: function(val, index) {
                                const label = this.getLabelForValue(val);
                                return label.length > 20 ? label.substr(0, 18) + '...' : label;
                            }
                        } 
                    },
                    y: { grid: { color: theme.grid }, ticks: { color: theme.text } }
                }
            }
        });
    }

    // Gráfico 3: Saldo por Região/UF e Competência (Linhas de Tendência)
    function renderRegionChart(data, selectedUf) {
        const ctx = document.getElementById('chart-hiring-by-region').getContext('2d');
        const theme = getThemeColors();

        if (chartHiringByRegion) {
            chartHiringByRegion.destroy();
        }

        const groups = [...new Set(data.map(item => item.regiao))];
        const competencies = [...new Set(data.map(item => item.competencia))].sort();

        const formatMonthYear = (dateStr) => {
            const date = new Date(dateStr);
            const months = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
            return `${months[date.getMonth()]}/${date.getFullYear().toString().substr(-2)}`;
        };

        const formattedLabels = competencies.map(formatMonthYear);

        const isLight = document.body.classList.contains('light-theme');
        const regionColors = {
            'SUDESTE': isLight ? '#4f46e5' : '#6366f1',
            'SUL': isLight ? '#059669' : '#10b981',
            'CENTRO-OESTE': '#d97706',
            'NORDESTE': '#2563eb',
            'NORTE': '#db2777'
        };

        const getGroupColor = (name) => {
            if (regionColors[name]) return regionColors[name];
            return isLight ? '#4f46e5' : '#6366f1';
        };

        const datasets = groups.map(group => {
            const points = competencies.map(comp => {
                const record = data.find(item => item.regiao === group && item.competencia === comp);
                return record ? record.saldo : 0;
            });

            return {
                label: group,
                data: points,
                borderColor: getGroupColor(group),
                backgroundColor: getGroupColor(group) + '15',
                borderWidth: 2.5,
                tension: 0.3,
                pointRadius: 4,
                pointHoverRadius: 6,
                fill: false
            };
        });

        chartHiringByRegion = new Chart(ctx, {
            type: 'line',
            data: {
                labels: formattedLabels,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { 
                        position: 'top',
                        labels: { color: theme.legend, font: { family: 'Inter', weight: '500' } }
                    },
                    tooltip: { backgroundColor: '#1e293b' }
                },
                scales: {
                    x: { grid: { color: theme.grid }, ticks: { color: theme.text } },
                    y: { grid: { color: theme.grid }, ticks: { color: theme.text } }
                }
            }
        });
    }

    // Função para buscar o status da última execução do ETL no backend
    async function loadEtlStatus() {
        try {
            const res = await fetch('/etl/status');
            if (res.status === 204 || res.status === 404) {
                return;
            }
            const statusData = await res.json();
            
            // Atualizar os campos de estatísticas na interface
            etlStatStatus.textContent = statusData.status || 'Nenhum';
            
            if (statusData.finalizadoEm) {
                const date = new Date(statusData.finalizadoEm);
                etlStatDate.textContent = date.toLocaleString('pt-BR');
            } else {
                etlStatDate.textContent = 'Sem data';
            }
            
            etlStatRead.textContent = (statusData.linhasLidas || 0).toLocaleString('pt-BR');
            etlStatProcessed.textContent = (statusData.linhasProcessadas || 0).toLocaleString('pt-BR');
            etlStatRejected.textContent = (statusData.linhasRejeitadas || 0).toLocaleString('pt-BR');

            // Atualizar o indicador visual do ETL
            etlStatusPulse.className = 'pulse-indicator';
            if (statusData.status === 'EM_ANDAMENTO') {
                etlStatusPulse.classList.add('status-running');
                btnTriggerEtl.disabled = true;
            } else if (statusData.status === 'SUCESSO') {
                etlStatusPulse.classList.add('status-idle');
                btnTriggerEtl.disabled = false;
            } else {
                etlStatusPulse.classList.add('status-error');
                btnTriggerEtl.disabled = false;
            }

        } catch (error) {
            console.error('Erro ao buscar status do ETL:', error);
        }
    }

    // Função para Disparar a carga de dados (ETL)
    async function triggerEtl() {
        btnTriggerEtl.disabled = true;
        etlSpinner.style.display = 'inline-block';
        
        etlStatusPulse.className = 'pulse-indicator status-running';
        etlStatStatus.textContent = 'EM_ANDAMENTO';
        
        try {
            const res = await fetch('/etl/run', { method: 'POST' });
            
            if (res.status === 202 || res.status === 200) {
                setTimeout(async () => {
                    await loadEtlStatus();
                    await loadDashboardData();
                    await loadTemporalRange();
                    etlSpinner.style.display = 'none';
                }, 1000);
            } else {
                alert('Erro ao iniciar o processamento ETL.');
                etlSpinner.style.display = 'none';
                await loadEtlStatus();
            }

        } catch (error) {
            console.error('Erro ao disparar carga do ETL:', error);
            alert('Falha na conexão com o servidor.');
            etlSpinner.style.display = 'none';
            await loadEtlStatus();
        }
    }
});
