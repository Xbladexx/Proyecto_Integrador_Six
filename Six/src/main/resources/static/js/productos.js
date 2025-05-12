document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const userInitial = document.getElementById('userInitial');

    // Elementos de la interfaz de productos
    const productsSearch = document.getElementById('productsSearch');
    const productsTableBody = document.getElementById('productsTableBody');
    const newProductButton = document.getElementById('newProductButton');

    // Elementos del modal
    const productModal = document.getElementById('productModal');
    const productModalTitle = document.getElementById('productModalTitle');
    const productCode = document.getElementById('productCode');
    const productName = document.getElementById('productName');
    const productCategory = document.getElementById('productCategory');
    const productStatus = document.getElementById('productStatus');
    const productDescription = document.getElementById('productDescription');
    const productPrice = document.getElementById('productPrice');
    const productCost = document.getElementById('productCost');
    const variantsContainer = document.getElementById('variantsContainer');
    const addVariantButton = document.getElementById('addVariantButton');
    const cancelProductButton = document.getElementById('cancelProductButton');
    const saveProductButton = document.getElementById('saveProductButton');
    const modalClose = document.querySelector('.modal-close');

    // Datos de ejemplo de productos
    const productsData = [{
            id: 1,
            code: "CAM-001",
            name: "Camiseta Slim Fit",
            category: "Camisetas",
            description: "Camiseta de algodón con corte slim fit",
            price: 79.9,
            cost: 39.95,
            variants: 4,
            status: "active"
        },
        {
            id: 2,
            code: "VES-001",
            name: "Vestido Casual",
            category: "Vestidos",
            description: "Vestido casual para uso diario",
            price: 129.9,
            cost: 64.95,
            variants: 3,
            status: "active"
        },
        {
            id: 3,
            code: "PAN-001",
            name: "Pantalón Chino",
            category: "Pantalones",
            description: "Pantalón chino de algodón",
            price: 99.9,
            cost: 49.95,
            variants: 6,
            status: "active"
        },
        {
            id: 4,
            code: "BLU-001",
            name: "Blusa Estampada",
            category: "Blusas",
            description: "Blusa con estampado floral",
            price: 89.9,
            cost: 44.95,
            variants: 4,
            status: "active"
        },
        {
            id: 5,
            code: "CHA-001",
            name: "Chaqueta Denim",
            category: "Chaquetas",
            description: "Chaqueta de mezclilla clásica",
            price: 159.9,
            cost: 79.95,
            variants: 3,
            status: "active"
        },
        {
            id: 6,
            code: "CAM-002",
            name: "Camiseta Estampada",
            category: "Camisetas",
            description: "Camiseta con estampado gráfico",
            price: 69.9,
            cost: 34.95,
            variants: 5,
            status: "active"
        },
        {
            id: 7,
            code: "PAN-002",
            name: "Pantalón Skinny",
            category: "Pantalones",
            description: "Pantalón skinny de algodón elástico",
            price: 109.9,
            cost: 54.95,
            variants: 8,
            status: "active"
        },
        {
            id: 8,
            code: "SUD-001",
            name: "Sudadera con Capucha",
            category: "Sudaderas",
            description: "Sudadera con capucha y bolsillo canguro",
            price: 119.9,
            cost: 59.95,
            variants: 6,
            status: "inactive"
        }
    ];

    // Variables de estado
    let selectedProduct = null;
    let editMode = false;

    // Mostrar inicial del usuario
    if (userInitial) {
        const usuario = userInitial.getAttribute('data-usuario') || 'A';
        userInitial.textContent = usuario.charAt(0).toUpperCase();
    }

    // Función para mostrar toast
    function showToast(title, message, type = 'success') {
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
    }

    // Cerrar toast al hacer clic en el botón de cerrar
    document.querySelector('.toast-close').addEventListener('click', function() {
        document.getElementById('toast').classList.add('hidden');
    });

    // Manejar clic en el botón de menú móvil
    mobileMenuButton.addEventListener('click', function() {
        sidebar.classList.toggle('open');
    });

    // Cerrar el sidebar al hacer clic fuera de él en dispositivos móviles
    document.addEventListener('click', function(event) {
        const isMobile = window.innerWidth <= 768;
        const isClickInsideSidebar = sidebar.contains(event.target);
        const isClickOnMenuButton = mobileMenuButton.contains(event.target);

        if (isMobile && sidebar.classList.contains('open') && !isClickInsideSidebar && !isClickOnMenuButton) {
            sidebar.classList.remove('open');
        }
    });

    // Funciones para la interfaz de productos

    // Cargar datos de productos
    function loadProductsData(data) {
        productsTableBody.innerHTML = '';

        if (data.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.innerHTML = `
                <td colspan="9" class="text-center">No se encontraron productos</td>
            `;
            productsTableBody.appendChild(emptyRow);
            return;
        }

        data.forEach(item => {
            const row = document.createElement('tr');

            // Determinar la clase de la insignia de estado
            const statusBadgeClass = item.status === 'active' ? 'status-active' : 'status-inactive';
            const statusText = item.status === 'active' ? 'Activo' : 'Inactivo';

            // Crear el contenido de la fila
            row.innerHTML = `
                <td>${item.code}</td>
                <td>${item.name}</td>
                <td>${item.category}</td>
                <td class="description-cell">${item.description}</td>
                <td>S/. ${item.price.toFixed(2)}</td>
                <td>S/. ${item.cost.toFixed(2)}</td>
                <td>${item.variants}</td>
                <td><span class="status-badge ${statusBadgeClass}">${statusText}</span></td>
                <td class="text-right">
                    <div class="dropdown">
                        <button class="action-button">
                            <i class="fas fa-ellipsis-h"></i>
                        </button>
                        <div class="dropdown-content">
                            <a href="#" class="edit-product" data-id="${item.id}">
                                <i class="fas fa-edit"></i> Editar producto
                            </a>
                            <a href="#" class="toggle-status" data-id="${item.id}" data-status="${item.status}">
                                ${item.status === 'active' ? '<i class="fas fa-ban"></i> Desactivar producto' : '<i class="fas fa-check"></i> Activar producto'}
                            </a>
                            <a href="#" class="delete-product text-red" data-id="${item.id}">
                                <i class="fas fa-trash"></i> Eliminar
                            </a>
                        </div>
                    </div>
                </td>
            `;

            productsTableBody.appendChild(row);

            // Agregar event listeners para las acciones
            const dropdown = row.querySelector('.dropdown');
            const actionButton = dropdown.querySelector('.action-button');

            actionButton.addEventListener('click', function(e) {
                e.stopPropagation();
                closeAllDropdowns();
                dropdown.classList.add('open');
            });

            const editButton = dropdown.querySelector('.edit-product');
            editButton.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = parseInt(this.dataset.id);
                openProductModal(productId, true);
            });

            const toggleStatusButton = dropdown.querySelector('.toggle-status');
            toggleStatusButton.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = parseInt(this.dataset.id);
                const currentStatus = this.dataset.status;
                toggleProductStatus(productId, currentStatus);
            });

            const deleteButton = dropdown.querySelector('.delete-product');
            deleteButton.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = parseInt(this.dataset.id);
                deleteProduct(productId);
            });
        });
    }

    // Cerrar todos los dropdowns
    function closeAllDropdowns() {
        document.querySelectorAll('.dropdown').forEach(dropdown => {
            dropdown.classList.remove('open');
        });
    }

    // Cerrar dropdowns al hacer clic fuera de ellos
    document.addEventListener('click', function() {
        closeAllDropdowns();
    });

    // Buscar productos
    productsSearch.addEventListener('input', function() {
        const searchTerm = this.value.trim().toLowerCase();

        if (searchTerm) {
            const filteredData = productsData.filter(item =>
                item.code.toLowerCase().includes(searchTerm) ||
                item.name.toLowerCase().includes(searchTerm) ||
                item.category.toLowerCase().includes(searchTerm) ||
                item.description.toLowerCase().includes(searchTerm)
            );

            loadProductsData(filteredData);
        } else {
            loadProductsData(productsData);
        }
    });

    // Abrir modal para nuevo producto
    newProductButton.addEventListener('click', function() {
        openProductModal();
    });

    // Abrir modal para editar/crear producto
    function openProductModal(productId = null, isEdit = false) {
        editMode = isEdit;

        // Limpiar el formulario
        productCode.value = '';
        productName.value = '';
        productCategory.value = '';
        productStatus.value = 'active';
        productDescription.value = '';
        productPrice.value = '';
        productCost.value = '';
        variantsContainer.innerHTML = '';

        if (isEdit && productId) {
            // Modo edición
            selectedProduct = productsData.find(item => item.id === productId);

            if (!selectedProduct) {
                showToast('Error', 'Producto no encontrado', 'error');
                return;
            }

            productModalTitle.textContent = 'Editar Producto';

            // Llenar el formulario con los datos del producto
            productCode.value = selectedProduct.code;
            productName.value = selectedProduct.name;
            productCategory.value = selectedProduct.category;
            productStatus.value = selectedProduct.status;
            productDescription.value = selectedProduct.description;
            productPrice.value = selectedProduct.price;
            productCost.value = selectedProduct.cost;

            // Simular variantes para el ejemplo
            for (let i = 0; i < selectedProduct.variants; i++) {
                addVariant({
                    color: i % 2 === 0 ? 'Negro' : 'Blanco',
                    size: ['S', 'M', 'L', 'XL'][i % 4],
                    stock: Math.floor(Math.random() * 20) + 1
                });
            }
        } else {
            // Modo creación
            selectedProduct = null;
            productModalTitle.textContent = 'Nuevo Producto';

            // Añadir una variante vacía por defecto
            addVariant();
        }

        // Mostrar el modal
        productModal.style.display = 'block';
    }

    // Añadir variante al formulario
    function addVariant(data = { color: '', size: '', stock: 1 }) {
        const variantItem = document.createElement('div');
        variantItem.className = 'variant-item';

        variantItem.innerHTML = `
            <div class="variant-color">
                <input type="text" class="form-control" placeholder="Color" value="${data.color}">
            </div>
            <div class="variant-size">
                <input type="text" class="form-control" placeholder="Talla" value="${data.size}">
            </div>
            <div class="variant-stock">
                <input type="number" class="form-control" placeholder="Stock" min="0" value="${data.stock}">
            </div>
            <div class="variant-actions">
                <button type="button" class="remove-variant">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        // Agregar event listener para eliminar variante
        variantItem.querySelector('.remove-variant').addEventListener('click', function() {
            variantItem.remove();
        });

        variantsContainer.appendChild(variantItem);
    }

    // Event listener para añadir variante
    addVariantButton.addEventListener('click', function() {
        addVariant();
    });

    // Cerrar modal
    function closeProductModal() {
        productModal.style.display = 'none';
        selectedProduct = null;
        editMode = false;
    }

    // Event listeners para el modal
    cancelProductButton.addEventListener('click', closeProductModal);
    modalClose.addEventListener('click', closeProductModal);

    // Cerrar modal al hacer clic fuera de él
    productModal.addEventListener('click', function(event) {
        if (event.target === productModal) {
            closeProductModal();
        }
    });

    // Guardar producto
    saveProductButton.addEventListener('click', function() {
        // Validar campos obligatorios
        if (!productCode.value || !productName.value || !productCategory.value || !productPrice.value || !productCost.value) {
            showToast('Error', 'Por favor complete todos los campos obligatorios', 'error');
            return;
        }

        // Contar variantes
        const variantItems = variantsContainer.querySelectorAll('.variant-item');
        if (variantItems.length === 0) {
            showToast('Error', 'Debe agregar al menos una variante', 'error');
            return;
        }

        // Crear objeto de producto
        const productData = {
            code: productCode.value,
            name: productName.value,
            category: productCategory.value,
            description: productDescription.value,
            price: parseFloat(productPrice.value),
            cost: parseFloat(productCost.value),
            variants: variantItems.length,
            status: productStatus.value
        };

        if (editMode && selectedProduct) {
            // Actualizar producto existente
            const productIndex = productsData.findIndex(item => item.id === selectedProduct.id);

            if (productIndex !== -1) {
                productsData[productIndex] = {
                    ...productsData[productIndex],
                    ...productData
                };

                showToast('Producto actualizado', `El producto ${productData.name} ha sido actualizado correctamente`);
            }
        } else {
            // Crear nuevo producto
            const newProduct = {
                id: productsData.length + 1,
                ...productData
            };

            productsData.push(newProduct);
            showToast('Producto creado', `El producto ${productData.name} ha sido creado correctamente`);
        }

        // Recargar la tabla y cerrar el modal
        loadProductsData(productsData);
        closeProductModal();
    });

    // Cambiar estado del producto
    function toggleProductStatus(productId, currentStatus) {
        const productIndex = productsData.findIndex(item => item.id === productId);

        if (productIndex === -1) {
            showToast('Error', 'Producto no encontrado', 'error');
            return;
        }

        const newStatus = currentStatus === 'active' ? 'inactive' : 'active';
        productsData[productIndex].status = newStatus;

        const statusText = newStatus === 'active' ? 'activado' : 'desactivado';
        showToast('Estado actualizado', `El producto ha sido ${statusText} correctamente`);

        // Recargar la tabla
        loadProductsData(productsData);
    }

    // Eliminar producto
    function deleteProduct(productId) {
        if (confirm('¿Está seguro de que desea eliminar este producto?')) {
            const productIndex = productsData.findIndex(item => item.id === productId);

            if (productIndex === -1) {
                showToast('Error', 'Producto no encontrado', 'error');
                return;
            }

            const productName = productsData[productIndex].name;
            productsData.splice(productIndex, 1);

            showToast('Producto eliminado', `El producto ${productName} ha sido eliminado correctamente`);

            // Recargar la tabla
            loadProductsData(productsData);
        }
    }

    // Inicializar la interfaz
    loadProductsData(productsData);
});