document.addEventListener('DOMContentLoaded', function() {
    // Función para cargar las alertas de stock
    function cargarAlertasStock() {
        fetch('/api/alertas-stock/critico')
            .then(response => response.json())
            .then(alertas => {
                const contenedorAlertas = document.querySelector('.stock-alerts');
                contenedorAlertas.innerHTML = ''; // Limpiar alertas existentes

                if (alertas.length === 0) {
                    contenedorAlertas.innerHTML = `
                        <div class="alert-item">
                            <div class="alert-details">
                                <p class="alert-title">No hay productos con stock crítico</p>
                            </div>
                        </div>
                    `;
                    return;
                }

                alertas.forEach(alerta => {
                    const alertaHTML = `
                        <div class="alert-item">
                            <div class="alert-icon">
                                <i class="fas fa-exclamation-triangle text-warning"></i>
                            </div>
                            <div class="alert-details">
                                <p class="alert-title">${alerta.producto} - ${alerta.variante}</p>
                                <div class="alert-badge critical">
                                    <i class="fas fa-arrow-down"></i> 
                                    Stock crítico: ${alerta.stockActual} unidades
                                </div>
                            </div>
                        </div>
                    `;
                    contenedorAlertas.innerHTML += alertaHTML;
                });
            })
            .catch(error => {
                console.error('Error al cargar alertas de stock:', error);
                const contenedorAlertas = document.querySelector('.stock-alerts');
                contenedorAlertas.innerHTML = `
                    <div class="alert-item">
                        <div class="alert-details">
                            <p class="alert-title">Error al cargar las alertas</p>
                        </div>
                    </div>
                `;
            });
    }

    // Cargar alertas inicialmente
    cargarAlertasStock();

    // Actualizar alertas cada 30 segundos
    setInterval(cargarAlertasStock, 30000);
});