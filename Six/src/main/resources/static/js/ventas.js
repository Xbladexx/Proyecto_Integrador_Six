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

    // Datos de ejemplo de productos
    const productsData = [{
            id: 1,
            code: "CAM-001",
            name: "Camiseta Slim Fit",
            color: "Negro",
            sizes: ["S", "M", "L", "XL"],
            price: 79.9,
            stock: 15
        },
        {
            id: 2,
            code: "CAM-002",
            name: "Camiseta Slim Fit",
            color: "Blanco",
            sizes: ["S", "M", "L"],
            price: 79.9,
            stock: 8
        },
        {
            id: 3,
            code: "VES-001",
            name: "Vestido Casual",
            color: "Azul",
            sizes: ["XS", "S", "M"],
            price: 129.9,
            stock: 2
        },
        {
            id: 4,
            code: "PAN-001",
            name: "Pantalón Chino",
            color: "Beige",
            sizes: ["28", "30", "32", "34"],
            price: 99.9,
            stock: 5
        },
        {
            id: 5,
            code: "BLU-001",
            name: "Blusa Estampada",
            color: "Multicolor",
            sizes: ["S", "M", "L"],
            price: 89.9,
            stock: 12
        },
        {
            id: 6,
            code: "CHA-001",
            name: "Chaqueta Denim",
            color: "Azul",
            sizes: ["M", "L", "XL", "XXL"],
            price: 159.9,
            stock: 4
        }
    ];

    // Variables de estado
    let selectedProduct = null;
    let cartItems = [];

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
        productSearchInput.addEventListener('input', function() {
            const searchTerm = this.value.trim().toLowerCase();

            if (searchTerm.length > 0) {
                const results = productsData.filter(product =>
                    product.code.toLowerCase().includes(searchTerm) ||
                    product.name.toLowerCase().includes(searchTerm) ||
                    product.color.toLowerCase().includes(searchTerm)
                );

                displaySearchResults(results);
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
                    <div class="result-code">${product.code}</div>
                    <div class="result-details">
                        <div class="result-name">${product.name}</div>
                        <div class="result-info">${product.color} - ${product.sizes.join(', ')}</div>
                    </div>
                    <div class="result-price">S/. ${product.price.toFixed(2)}</div>
                `;

                resultItem.addEventListener('click', () => {
                    selectProduct(product);
                });

                searchResults.appendChild(resultItem);
            });

            searchResults.classList.remove('hidden');
        } else {
            searchResults.classList.add('hidden');
            resetProductSelection();
        }
    }

    // Buscar producto al hacer clic en el botón de búsqueda
    if (searchButton) {
        searchButton.addEventListener('click', function() {
            const searchTerm = productSearchInput.value.trim();

            if (searchTerm) {
                const foundProduct = productsData.find(product =>
                    product.code.toLowerCase() === searchTerm.toLowerCase() ||
                    product.name.toLowerCase().includes(searchTerm.toLowerCase())
                );

                if (foundProduct) {
                    selectProduct(foundProduct);
                } else {
                    showToast('Producto no encontrado', 'No se encontró ningún producto con ese código o nombre', 'error');
                }
            } else {
                showToast('Campo vacío', 'Por favor ingrese un código o nombre de producto', 'error');
            }
        });
    }

    // Seleccionar producto
    function selectProduct(product) {
        selectedProduct = product;
        productSearchInput.value = `${product.code} - ${product.name} - ${product.color}`;

        // Actualizar el select de tallas
        sizeSelect.innerHTML = '<option value="">Talla</option>';
        product.sizes.forEach(size => {
            const option = document.createElement('option');
            option.value = size;
            option.textContent = size;
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
                    code: selectedProduct.code,
                    name: selectedProduct.name,
                    color: selectedProduct.color,
                    size: selectedSize,
                    price: selectedProduct.price,
                    quantity: quantity
                });
            }

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
        const igv = subtotal * 0.08;
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

    // Procesar pago
    if (processPaymentButton) {
        processPaymentButton.addEventListener('click', function() {
            if (cartItems.length === 0) {
                showToast('Carrito vacío', 'Agregue productos al carrito para procesar el pago', 'error');
                return;
            }

            if (!customerNameInput.value || !customerDNIInput.value) {
                showToast('Datos incompletos', 'Por favor ingrese el nombre y DNI del cliente', 'error');
                return;
            }

            // Simular procesamiento de pago
            showToast('Procesando', 'Procesando pago...', 'success');

            setTimeout(() => {
                // Aquí se enviaría la información al servidor
                showToast('Éxito', 'Venta registrada correctamente', 'success');

                // Limpiar formulario
                cartItems = [];
                updateCartUI();
                updateTotals();
                customerNameInput.value = '';
                customerDNIInput.value = '';
            }, 1500);
        });
    }

    // Inicializar la interfaz
    updateCartUI();
});