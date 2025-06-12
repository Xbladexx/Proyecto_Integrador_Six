/**
 * Script para manejar el cambio de pestañas en reportes
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Tab Switcher inicializado');
    
    // Referencias a elementos
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    
    console.log('Botones de pestañas encontrados:', tabButtons.length);
    console.log('Contenidos de pestañas encontrados:', tabContents.length);
    
    // Función para cambiar de pestaña
    function switchTab(tabId) {
        console.log('Cambiando a pestaña:', tabId);
        
        // Desactivar todas las pestañas
        tabButtons.forEach(btn => btn.classList.remove('active'));
        tabContents.forEach(content => content.classList.remove('active'));
        
        // Activar la pestaña seleccionada
        const selectedButton = document.querySelector(`.tab-button[data-tab="${tabId}"]`);
        const selectedContent = document.getElementById(`${tabId}-tab`);
        
        if (selectedButton) {
            selectedButton.classList.add('active');
            console.log('Botón activado:', selectedButton.textContent.trim());
        } else {
            console.error('No se encontró el botón para la pestaña:', tabId);
        }
        
        if (selectedContent) {
            selectedContent.classList.add('active');
            console.log('Contenido activado:', selectedContent.id);
        } else {
            console.error('No se encontró el contenido para la pestaña:', tabId);
        }
        
        // Actualizar gráficos específicos de la pestaña
        if (window.updateCharts) {
            console.log('Llamando a updateCharts para la pestaña:', tabId);
            try {
                window.updateCharts(tabId);
            } catch (error) {
                console.error('Error al actualizar gráficos:', error);
            }
        } else {
            console.warn('La función updateCharts no está disponible');
        }
        
        // Disparar evento personalizado para que otros scripts puedan reaccionar
        const event = new CustomEvent('tabChanged', { detail: { tabId: tabId } });
        document.dispatchEvent(event);
    }
    
    // Asignar eventos a los botones de pestañas
    tabButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            const tabId = this.dataset.tab;
            switchTab(tabId);
        });
    });
    
    // Exponer función para uso global
    window.switchTab = switchTab;
    
    // Verificar que la pestaña activa sea correcta al cargar la página
    setTimeout(() => {
        const activeButton = document.querySelector('.tab-button.active');
        if (activeButton) {
            const tabId = activeButton.dataset.tab;
            const tabContent = document.getElementById(`${tabId}-tab`);
            
            if (!tabContent || !tabContent.classList.contains('active')) {
                console.warn('Inconsistencia detectada en pestañas, corrigiendo...');
                switchTab(tabId);
            }
        }
    }, 500);
}); 