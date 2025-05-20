document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const productSearchInput = document.getElementById('productSearch');
    const searchButton = document.getElementById('searchButton');
    const searchResults = document.getElementById('searchResults');
    const sizeSelect = document.getElementById('sizeSelect');
    const quantityInput = document.getElementById('quantity');
    const decreaseQuantityButton = document.getElementById('decreaseQuantity');
    const increaseQuantityButton = document.getElementById('increaseQuantity');
    const addToCartButton = document.getElementById('addToCartButton');
    const cartItemsContainer = document.getElementById('cartItems');
    const subtotalElement = document.getElementById('subtotal');
    const igvElement = document.getElementById('igv');
    const totalElement = document.getElementById('total');
    const customerNameInput = document.getElementById('customerName');
    const customerDNIInput = document.getElementById('customerDNI');
    const clearCartButton = document.getElementById('clearCartButton');
    const processPaymentButton = document.getElementById('processPaymentButton');
    const toastCloseButton = document.querySelector('.toast-close');

    // Datos de productos cargados desde la API
    let productsData = [];

    // Variables de estado
    let selectedProduct = null;
    let cartItems = [];

    // Cargar productos al iniciar
    loadProducts();

    // Función para cargar productos desde la API
    function loadProducts() {
        fetch('/api/productos/buscar-ventas')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar productos');
                }
                return response.json();
            })
            .then(data => {
                productsData = data;
                console.log('Productos cargados:', productsData.length);
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Error', 'No se pudieron cargar los productos', 'error');
            });
    }

    // Función para mostrar toast
    window.showToast = function(title, message, type = 'success') {
        const toast = document.getElementById('toast');
        const toastTitle = toast.querySelector('.toast-title');
        const toastMessage = toast.querySelector('.toast-message');

        toastTitle.textContent = title;
        toastMessage.textContent = message;

        toast.classList.remove('hidden', 'success', 'error');
        toast.classList.add(type);

        // Ocultar el toast después de 5 segundos
        setTimeout(() => {
            toast.classList.add('hidden');
        }, 5000);
    };

    // Cerrar toast al hacer clic en el botón de cerrar
    if (toastCloseButton) {
        toastCloseButton.addEventListener('click', function() {
            document.getElementById('toast').classList.add('hidden');
        });
    }

    // Manejar clic en el botón de menú móvil
    if (mobileMenuButton) {
        mobileMenuButton.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
    }

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton && mobileMenuButton.contains(event.target);

        if (isMobile && sidebar.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });

    // Funciones para la interfaz de ventas

    // Buscar productos mientras se escribe
    if (productSearchInput) {
        let debounceTimer;

        productSearchInput.addEventListener('input', function() {
            const searchTerm = this.value.trim().toLowerCase();

            // Limpiar el temporizador anterior
            clearTimeout(debounceTimer);

            if (searchTerm.length > 0) {
                // Buscar en productos locales primero para respuesta instantánea
                const localResults = productsData.filter(product =>
                    product.codigo.toLowerCase().includes(searchTerm) ||
                    product.nombre.toLowerCase().includes(searchTerm) ||
                    product.color.toLowerCase().includes(searchTerm)
                );

                // Siempre mostrar resultados locales inmediatamente
                displaySearchResults(localResults);

                // Usar debounce para evitar múltiples llamadas al servidor mientras el usuario escribe
                if (searchTerm.length >= 3) {
                    debounceTimer = setTimeout(() => {
                        fetch(`/api/productos/buscar-ventas?query=${encodeURIComponent(searchTerm)}`)
                            .then(response => {
                                if (!response.ok) {
                                    throw new Error('Error en la respuesta del servidor: ' + response.status);
                                }
                                return response.json();
                            })
                            .then(data => {
                                if (data && data.length > 0) {
                                    // Solo actualizar productsData si hay resultados
                                    productsData = [...data];

                                    // Verificar que el término de búsqueda aún coincide con el valor actual
                                    // para evitar resultados incorrectos si el usuario cambió el texto
                                    if (productSearchInput.value.trim().toLowerCase().includes(searchTerm)) {
                                        displaySearchResults(data);
                                    }
                                }
                            })
                            .catch(error => console.error('Error al buscar productos:', error));
                    }, 300); // Esperar 300ms después de que el usuario deje de escribir
                }
            } else {
                searchResults.classList.add('hidden');
            }
        });
    }

    // Mostrar resultados de búsqueda
    function displaySearchResults(results) {
        searchResults.innerHTML = '';

        if (results.length > 0) {
            results.forEach(product => {
                const resultItem = document.createElement('div');
                resultItem.className = 'search-result-item';
                resultItem.innerHTML = `
                    <div class="result-code">${product.codigo}</div>
                    <div class="result-details">
                        <div class="result-name">${product.nombre}</div>
                        <div class="result-info">${product.color} - ${product.tallasDisponibles.join(', ')}</div>
                    </div>
                    <div class="result-price">S/. ${product.precio.toFixed(2)}</div>
                `;

                resultItem.addEventListener('click', () => {
                    selectProduct(product);
                });

                searchResults.appendChild(resultItem);
            });

            // Asegurarse de que los resultados siempre sean visibles cuando hay coincidencias
            searchResults.classList.remove('hidden');
        } else {
            // Solo ocultar resultados si no hay coincidencias y el campo está vacío
            if (!productSearchInput.value.trim()) {
                searchResults.classList.add('hidden');
                resetProductSelection();
            } else {
                // Si hay texto escrito pero sin resultados, mostrar mensaje
                searchResults.innerHTML = '<div class="no-results">No se encontraron productos</div>';
                searchResults.classList.remove('hidden');
            }
        }
    }

    // Buscar producto al hacer clic en el botón de búsqueda
    if (searchButton) {
        searchButton.addEventListener('click', function() {
            const searchTerm = productSearchInput.value.trim();

            if (searchTerm) {
                // Buscar en el servidor
                fetch(`/api/productos/buscar-ventas?query=${encodeURIComponent(searchTerm)}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error en la respuesta del servidor: ' + response.status);
                        }
                        return response.json();
                    })
                    .then(data => {
                        // Solo actualizar si hay resultados para no perder el contexto actual
                        if (data && data.length > 0) {
                            productsData = [...data];
                            displaySearchResults(data);
                        } else {
                            // Mostrar mensaje de "no encontrado" en el área de resultados
                            searchResults.innerHTML = '<div class="no-results">No se encontraron productos</div>';
                            searchResults.classList.remove('hidden');
                            showToast('Producto no encontrado', 'No se encontró ningún producto con ese código o nombre', 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showToast('Error', 'Error al buscar productos: ' + error.message, 'error');
                    });
            } else {
                showToast('Campo vacío', 'Por favor ingrese un código o nombre de producto', 'error');
            }
        });
    }

    // Seleccionar producto
    function selectProduct(product) {
        selectedProduct = product;
        productSearchInput.value = `${product.codigo} - ${product.nombre} - ${product.color}`;

        console.log('Producto seleccionado:', product);

        // Actualizar el select de tallas
        sizeSelect.innerHTML = '<option value="">Talla</option>';

        // Preseleccionar la talla específica de esta variante si está disponible
        let tallaActual = product.talla;

        product.tallasDisponibles.forEach(size => {
            const option = document.createElement('option');
            option.value = size;
            option.textContent = size;

            // Si es la talla actual de la variante, seleccionarla
            if (size === tallaActual) {
                option.selected = true;
            }

            sizeSelect.appendChild(option);
        });

        // Habilitar controles
        sizeSelect.disabled = false;
        decreaseQuantityButton.disabled = false;
        increaseQuantityButton.disabled = false;

        // Resetear cantidad
        quantityInput.value = 1;

        // Ocultar resultados
        searchResults.classList.add('hidden');

        // Actualizar estado del botón
        updateAddButtonState();
    }

    // Manejar cambio de talla
    if (sizeSelect) {
        sizeSelect.addEventListener('change', function() {
            updateAddButtonState();
        });
    }

    // Incrementar cantidad
    if (increaseQuantityButton) {
        increaseQuantityButton.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value);
            if (currentValue < selectedProduct.stock) {
                quantityInput.value = currentValue + 1;
            } else {
                showToast('Stock insuficiente', 'No hay suficiente stock disponible', 'error');
            }
        });
    }

    // Decrementar cantidad
    if (decreaseQuantityButton) {
        decreaseQuantityButton.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value);
            if (currentValue > 1) {
                quantityInput.value = currentValue - 1;
            }
        });
    }

    // Validar entrada de cantidad
    if (quantityInput) {
        quantityInput.addEventListener('change', function() {
            let value = parseInt(this.value);

            if (isNaN(value) || value < 1) {
                value = 1;
            } else if (selectedProduct && value > selectedProduct.stock) {
                value = selectedProduct.stock;
                showToast('Stock insuficiente', 'No hay suficiente stock disponible', 'error');
            }

            this.value = value;
        });
    }

    // Actualizar estado del botón Agregar
    function updateAddButtonState() {
        const sizeSelected = sizeSelect.value !== '';
        addToCartButton.disabled = !selectedProduct || !sizeSelected;
    }

    // Función para buscar una variante específica por talla
    function buscarVariantePorTalla(productoId, color, talla) {
        // Esta función intenta encontrar en productsData la variante que corresponde a la talla seleccionada
        for (const producto of productsData) {
            if (producto.id === productoId && producto.color === color && producto.talla === talla) {
                return producto.varianteId;
            }
        }
        return null;
    }

    // Agregar producto al carrito
    if (addToCartButton) {
        addToCartButton.addEventListener('click', function() {
            if (!selectedProduct) {
                showToast('Error', 'Selecciona un producto primero', 'error');
                return;
            }

            const selectedSize = sizeSelect.value;
            if (!selectedSize) {
                showToast('Error', 'Selecciona una talla', 'error');
                return;
            }

            const quantity = parseInt(quantityInput.value);
            if (isNaN(quantity) || quantity < 1) {
                showToast('Error', 'Cantidad no válida', 'error');
                return;
            }

            // Encontrar la variante correcta por talla
            let varianteId = selectedProduct.varianteId;

            // Si la talla seleccionada es diferente a la talla de la variante actual,
            // intentar buscar la variante correcta
            if (selectedSize !== selectedProduct.talla) {
                const varianteEncontradaId = buscarVariantePorTalla(selectedProduct.id, selectedProduct.color, selectedSize);
                if (varianteEncontradaId) {
                    varianteId = varianteEncontradaId;
                    console.log('Se encontró una variante específica para la talla seleccionada:', varianteId);
                } else {
                    console.warn('No se encontró una variante específica para la talla seleccionada, usando la variante principal');
                }
            }

            // Verificar si el producto ya está en el carrito con la misma talla
            const existingItemIndex = cartItems.findIndex(item =>
                item.id === selectedProduct.id && item.size === selectedSize
            );

            if (existingItemIndex !== -1) {
                // Actualizar cantidad
                cartItems[existingItemIndex].quantity += quantity;
            } else {
                // Agregar nuevo item
                cartItems.push({
                    id: selectedProduct.id,
                    varianteId: varianteId, // Guardar explícitamente el ID de la variante
                    code: selectedProduct.codigo,
                    name: selectedProduct.nombre,
                    color: selectedProduct.color,
                    size: selectedSize,
                    price: selectedProduct.precio,
                    quantity: quantity
                });
            }

            console.log('Producto agregado al carrito:',
                'ID:', selectedProduct.id,
                'Nombre:', selectedProduct.nombre,
                'Talla:', selectedSize,
                'Variante ID:', varianteId);

            // Actualizar UI
            updateCartUI();
            updateTotals();

            // Resetear selección
            resetProductSelection();

            showToast('Éxito', 'Producto agregado al carrito', 'success');
        });
    }

    // Actualizar la interfaz del carrito
    function updateCartUI() {
        cartItemsContainer.innerHTML = '';

        if (cartItems.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.className = 'empty-cart';
            emptyRow.innerHTML = '<td colspan="8" class="text-center">No hay productos en el carrito</td>';
            cartItemsContainer.appendChild(emptyRow);
            return;
        }

        cartItems.forEach((item, index) => {
            const row = document.createElement('tr');
            const subtotal = item.price * item.quantity;

            row.innerHTML = `
                <td>${item.code}</td>
                <td>${item.name}</td>
                <td>${item.color}</td>
                <td>${item.size}</td>
                <td>S/. ${item.price.toFixed(2)}</td>
                <td>${item.quantity}</td>
                <td>S/. ${subtotal.toFixed(2)}</td>
                <td>
                    <button class="btn-icon delete-item" data-index="${index}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
            `;

            cartItemsContainer.appendChild(row);
        });

        // Agregar evento a los botones de eliminar
        document.querySelectorAll('.delete-item').forEach(button => {
            button.addEventListener('click', function() {
                const index = parseInt(this.getAttribute('data-index'));
                removeCartItem(index);
            });
        });
    }

    // Eliminar producto del carrito
    function removeCartItem(index) {
        cartItems.splice(index, 1);
        updateCartUI();
        updateTotals();
        showToast('Producto eliminado', 'El producto ha sido eliminado del carrito');
    }

    // Actualizar totales
    function updateTotals() {
        const subtotal = cartItems.reduce((total, item) => total + (item.price * item.quantity), 0);
        const igv = subtotal * 0.18;
        const total = subtotal + igv;

        subtotalElement.textContent = `S/. ${subtotal.toFixed(2)}`;
        igvElement.textContent = `S/. ${igv.toFixed(2)}`;
        totalElement.textContent = `S/. ${total.toFixed(2)}`;
    }

    // Resetear selección de producto
    function resetProductSelection() {
        selectedProduct = null;
        productSearchInput.value = '';
        sizeSelect.innerHTML = '<option value="">Talla</option>';
        sizeSelect.disabled = true;
        quantityInput.value = 1;
        decreaseQuantityButton.disabled = true;
        increaseQuantityButton.disabled = true;
        addToCartButton.disabled = true;
    }

    // Limpiar carrito
    if (clearCartButton) {
        clearCartButton.addEventListener('click', function() {
            if (cartItems.length === 0) {
                showToast('Carrito vacío', 'No hay productos para eliminar', 'error');
                return;
            }

            cartItems = [];
            updateCartUI();
            updateTotals();
            showToast('Carrito limpiado', 'Se han eliminado todos los productos del carrito', 'success');
        });
    }

    // Verificar si el cliente ya existe por DNI
    function verificarClienteExistente(dni) {
        if (!dni) return;

        fetch(`/api/clientes/buscar?dni=${encodeURIComponent(dni)}`)
            .then(response => response.json())
            .then(cliente => {
                if (cliente && cliente.id) {
                    // Cliente encontrado, mostrar sus datos
                    customerNameInput.value = cliente.nombre;
                    console.log('Cliente encontrado:', cliente.nombre);
                }
            })
            .catch(error => {
                console.error('Error al verificar cliente:', error);
            });
    }

    // Agregar evento para verificar cliente al ingresar DNI
    if (customerDNIInput) {
        // Validar que solo se ingresen números y limitar a 8 caracteres
        customerDNIInput.addEventListener('input', function(e) {
            // Reemplazar cualquier carácter que no sea un número
            this.value = this.value.replace(/[^0-9]/g, '');

            // Limitar a 8 caracteres
            if (this.value.length > 8) {
                this.value = this.value.slice(0, 8);
            }

            // Aplicar estilo visual para indicar validez
            if (this.value.length === 8) {
                this.classList.remove('invalid-input');
                this.classList.add('valid-input');
            } else if (this.value.length > 0) {
                this.classList.remove('valid-input');
                this.classList.add('invalid-input');
            } else {
                this.classList.remove('valid-input', 'invalid-input');
            }
        });

        customerDNIInput.addEventListener('blur', function() {
            // Validar al perder el foco
            if (this.value.trim().length > 0 && this.value.trim().length !== 8) {
                showToast('DNI inválido', 'El DNI debe contener exactamente 8 números', 'error');
            } else if (this.value.trim().length === 8) {
                verificarClienteExistente(this.value.trim());
            }
        });
    }

    // Procesar pago
    if (processPaymentButton) {
        processPaymentButton.addEventListener('click', function() {
            if (cartItems.length === 0) {
                showToast('Carrito vacío', 'Agregue productos al carrito para procesar el pago', 'error');
                return;
            }

            const nombreCliente = customerNameInput.value.trim();
            const dniCliente = customerDNIInput.value.trim();

            if (!nombreCliente || !dniCliente) {
                showToast('Datos incompletos', 'Por favor ingrese el nombre y DNI del cliente', 'error');
                return;
            }

            // Validar que el DNI tiene exactamente 8 números
            const dniRegex = /^\d{8}$/;
            if (!dniRegex.test(dniCliente)) {
                showToast('DNI inválido', 'El DNI debe contener exactamente 8 números', 'error');
                customerDNIInput.focus();
                return;
            }

            // Preparar los datos de la venta
            const subtotal = parseFloat(subtotalElement.textContent.replace('S/. ', ''));
            const igv = parseFloat(igvElement.textContent.replace('S/. ', ''));
            const total = parseFloat(totalElement.textContent.replace('S/. ', ''));

            // Convertir items del carrito al formato esperado por el backend
            const items = cartItems.map(item => {
                console.log('Item para venta:', item);
                // Asegurarse de que varianteId sea el correcto
                let varianteId = item.varianteId || item.id;

                return {
                    varianteId: varianteId,
                    codigo: item.code,
                    nombre: item.name,
                    color: item.color,
                    talla: item.size,
                    cantidad: item.quantity,
                    precioUnitario: item.price,
                    subtotal: item.price * item.quantity
                };
            });

            const ventaData = {
                nombreCliente: nombreCliente,
                dniCliente: dniCliente,
                subtotal: subtotal,
                igv: igv,
                total: total,
                items: items
            };

            console.log('Datos de venta a enviar:', ventaData);

            // Mostrar toast de procesamiento
            showToast('Procesando', 'Registrando venta en el sistema...', 'success');

            // Deshabilitar el botón para evitar clics múltiples
            processPaymentButton.disabled = true;

            // Enviar los datos al servidor
            fetch('/api/ventas', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(ventaData)
                })
                .then(response => {
                    processPaymentButton.disabled = false;

                    if (!response.ok) {
                        return response.text().then(errorMsg => {
                            throw new Error(errorMsg || 'Error al procesar la venta');
                        });
                    }
                    return response.text();
                })
                .then(codigoVenta => {
                    showToast('Éxito', `Venta registrada correctamente con código: ${codigoVenta}`, 'success');

                    // Imprimir ticket (simulado)
                    console.log('Imprimiendo ticket para venta:', codigoVenta);

                    // Limpiar formulario
                    cartItems = [];
                    updateCartUI();
                    updateTotals();
                    customerNameInput.value = '';
                    customerDNIInput.value = '';
                })
                .catch(error => {
                    console.error('Error:', error);
                    processPaymentButton.disabled = false;
                    showToast('Error', error.message || 'No se pudo procesar la venta. Inténtelo nuevamente.', 'error');
                });
        });
    }

    // Inicializar la interfaz
    updateCartUI();
});