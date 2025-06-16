document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const sidebar = document.querySelector('.sidebar');
    const mobileMenuButton = document.querySelector('.mobile-menu-button');
    const userInitial = document.getElementById('userInitial');

    // Elementos de la interfaz de productos
    const productsSearch = document.getElementById('productsSearch');
    const productsTableBody = document.getElementById('productsTableBody');
    const newProductButton = document.getElementById('newProductButton');
    const newCategoryButton = document.getElementById('newCategoryButton');

    // Elementos del modal de productos
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
    const productModalClose = productModal.querySelector('.modal-close');

    // Elementos del modal de categorías
    const categoryModal = document.getElementById('categoryModal');
    const categoryModalTitle = document.getElementById('categoryModalTitle');
    const categoryName = document.getElementById('categoryName');
    const categoryDescription = document.getElementById('categoryDescription');
    const categoryStatus = document.getElementById('categoryStatus');
    const cancelCategoryButton = document.getElementById('cancelCategoryButton');
    const saveCategoryButton = document.getElementById('saveCategoryButton');
    const categoryModalClose = categoryModal.querySelector('.modal-close');

    // Datos de productos 
    let productsData = [];
    let categoriesData = [];

    // Variables de estado
    let selectedProduct = null;
    let editMode = false;
    let selectedCategory = null;
    let categoryEditMode = false;

    // Mostrar inicial del usuario
    if (userInitial) {
        const usuario = userInitial.getAttribute('data-usuario') || 'A';
        userInitial.textContent = usuario.charAt(0).toUpperCase();
    }

    // Función para realizar una solicitud fetch con timeout y manejo mejorado de errores
    function fetchWithTimeout(url, options = {}, timeout = 10000) {
        console.log(`Realizando fetch a: ${url}`);
        
        return Promise.race([
            fetch(url, options),
            new Promise((_, reject) =>
                setTimeout(() => reject(new Error(`Timeout al intentar conectar con ${url}`)), timeout)
            )
        ]);
    }

    // Verificar estado de la API antes de intentar cargar datos
    function checkApiHealth() {
        console.log('Verificando estado de la API...');
        
        fetchWithTimeout('/api/health')
            .then(response => {
                console.log('Respuesta de health check:', response.status, response.statusText);
                if (!response.ok) {
                    throw new Error(`API no disponible: ${response.status} ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Estado de la API:', data);
                
                if (data.status === 'UP') {
                    console.log(`API funcionando correctamente. Categorías: ${data.categorias}, Productos: ${data.productos}`);
                    
                    // La API está funcionando, cargar datos
                    fetchCategories();
                } else {
                    throw new Error(`API no disponible: ${data.message}`);
                }
            })
            .catch(error => {
                console.error('Error al verificar estado de la API:', error);
                showToast('Error', `No se pudo conectar con el servidor: ${error.message}. Usando datos locales.`, 'error');
                
                // Usar datos locales en caso de error
                categoriesData = [
                    { id: 1, nombre: 'Camisetas' },
                    { id: 2, nombre: 'Pantalones' },
                    { id: 3, nombre: 'Vestidos' },
                    { id: 4, nombre: 'Chaquetas' },
                    { id: 5, nombre: 'Blusas' },
                    { id: 6, nombre: 'Accesorios' }
                ];
                
                populateCategorySelect();
                
                productsData = [{
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
                }];
                
                loadProductsData(productsData);
            });
    }

    // Cargar categorías desde la API
    function fetchCategories() {
        console.log('Intentando cargar categorías...');
        
        fetchWithTimeout('/api/categorias')
            .then(response => {
                console.log('Respuesta de categorías:', response.status, response.statusText);
                if (!response.ok) {
                    throw new Error(`Error al cargar categorías: ${response.status} ${response.statusText}`);
                }
                return response.text(); // Primero obtener como texto para verificar
            })
            .then(text => {
                console.log('Texto recibido de la API:', text.substring(0, 100) + '...');
                
                // Intentar parsear el texto como JSON
                try {
                    // Limpiar el texto en caso de tener caracteres no válidos
                    let cleanText = text;
                    
                    // Si el texto contiene caracteres de control, los eliminamos
                    const jsonStartPos = text.indexOf('[');
                    const jsonEndPos = text.lastIndexOf(']') + 1;
                    
                    if (jsonStartPos >= 0 && jsonEndPos > jsonStartPos) {
                        cleanText = text.substring(jsonStartPos, jsonEndPos);
                        console.log('Extrayendo JSON:', cleanText.substring(0, 50) + '...');
                    }
                    
                    // Intentar parsear el JSON limpio
                    const data = JSON.parse(cleanText);
                    console.log('Categorías cargadas:', data);
                    
                    if (!data || data.length === 0) {
                        console.warn('No se encontraron categorías en la base de datos');
                        showToast('Advertencia', 'No hay categorías definidas en la base de datos', 'error');
                        
                        // Crear categorías predeterminadas para poder continuar
                        categoriesData = [
                            { id: 1, nombre: 'Camisetas' },
                            { id: 2, nombre: 'Pantalones' },
                            { id: 3, nombre: 'Vestidos' },
                            { id: 4, nombre: 'Chaquetas' },
                            { id: 5, nombre: 'Blusas' },
                            { id: 6, nombre: 'Accesorios' }
                        ];
                    } else {
                        categoriesData = data;
                    }
                    
                    // Actualizar el select de categorías
                    populateCategorySelect();
                    
                    // Una vez cargadas las categorías, cargar los productos
                    fetchProducts();
                } catch (err) {
                    console.error('Error al parsear JSON de categorías:', err);
                    console.error('Texto que causó el error:', text);
                    showToast('Error', `Error al procesar las categorías: ${err.message}. Usando categorías por defecto.`, 'error');
                    
                    // Crear categorías predeterminadas si hay error
                    categoriesData = [
                        { id: 1, nombre: 'Camisetas' },
                        { id: 2, nombre: 'Pantalones' },
                        { id: 3, nombre: 'Vestidos' },
                        { id: 4, nombre: 'Chaquetas' },
                        { id: 5, nombre: 'Blusas' },
                        { id: 6, nombre: 'Accesorios' }
                    ];
                    
                    populateCategorySelect();
                    fetchProducts();
                }
            })
            .catch(error => {
                console.error('Error al cargar categorías:', error);
                showToast('Error', `No se pudieron cargar las categorías: ${error.message}. Usando categorías por defecto.`, 'error');
                
                // Crear categorías predeterminadas si hay error
                categoriesData = [
                    { id: 1, nombre: 'Camisetas' },
                    { id: 2, nombre: 'Pantalones' },
                    { id: 3, nombre: 'Vestidos' },
                    { id: 4, nombre: 'Chaquetas' },
                    { id: 5, nombre: 'Blusas' },
                    { id: 6, nombre: 'Accesorios' }
                ];
                
                populateCategorySelect();
                fetchProducts();
            });
    }

    // Poblar el select de categorías
    function populateCategorySelect() {
        // Mantener la opción por defecto
        productCategory.innerHTML = '<option value="">Seleccionar categoría</option>';
        
        // Añadir las categorías
        categoriesData.forEach(category => {
            const option = document.createElement('option');
            option.value = category.nombre;
            option.textContent = category.nombre;
            productCategory.appendChild(option);
        });
    }

    // Cargar productos desde la API
    function fetchProducts() {
        console.log('Intentando cargar productos...');
        
        // Mostrar un indicador de carga en la tabla
        productsTableBody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center">
                    <i class="fas fa-spinner fa-spin"></i> Cargando productos...
                </td>
            </tr>
        `;
        
        fetchWithTimeout('/api/productos')
            .then(response => {
                console.log('Respuesta de productos:', response.status, response.statusText);
                if (!response.ok) {
                    throw new Error(`Error al cargar productos: ${response.status} ${response.statusText}`);
                }
                return response.text(); // Primero obtener como texto para verificar
            })
            .then(text => {
                console.log('Texto recibido de la API de productos:', text.substring(0, 100) + '...');
                
                try {
                    // Limpiar el texto en caso de tener caracteres no válidos
                    let cleanText = text;
                    
                    // Si el texto contiene caracteres de control, los eliminamos
                    const jsonStartPos = text.indexOf('[');
                    const jsonEndPos = text.lastIndexOf(']') + 1;
                    
                    if (jsonStartPos >= 0 && jsonEndPos > jsonStartPos) {
                        cleanText = text.substring(jsonStartPos, jsonEndPos);
                        console.log('Extrayendo JSON de productos:', cleanText.substring(0, 50) + '...');
                    }
                    
                    // Intentar parsear el JSON limpio
                    const data = JSON.parse(cleanText);
                    console.log('Productos cargados:', data);
                    
                    // Si no hay datos, mostrar mensaje específico
                    if (!data || data.length === 0) {
                        console.log('No se encontraron productos en la base de datos');
                        showToast('Información', 'No se encontraron productos en la base de datos', 'error');
                        loadProductsData([]);
                        return;
                    }
                    
                    productsData = data.map(item => {
                        console.log('Procesando producto:', item.nombre);
                        console.log('Variantes en este producto:', item.variantes ? item.variantes.length : 0);
                        if (item.variantes) {
                            item.variantes.forEach((v, i) => console.log(`  Variante ${i+1}:`, v.color, v.talla, v.id));
                        }
                        
                        return {
                            id: item.id,
                            code: item.codigo || '',
                            name: item.nombre || '',
                            category: item.categoria ? item.categoria.nombre : '',
                            categoryId: item.categoria ? item.categoria.id : null,
                            description: item.descripcion || '',
                            price: item.precio || 0,
                            cost: item.costo || 0,
                            variants: item.variantes ? item.variantes.length : 0,
                            status: item.estado === 'ACTIVO' ? 'active' : 'inactive'
                        };
                    });
                    loadProductsData(productsData);
                } catch (err) {
                    console.error('Error al parsear JSON de productos:', err);
                    console.error('Texto que causó el error:', text);
                    showToast('Error', `Error al procesar los productos: ${err.message}. Usando datos de ejemplo.`, 'error');
                    
                    // Crear datos de ejemplo si hay error
                    productsData = [{
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
                    }];
                    
                    loadProductsData(productsData);
                }
            })
            .catch(error => {
                console.error('Error al cargar productos:', error);
                showToast('Error', `No se pudieron cargar los productos: ${error.message}. Por favor intente de nuevo o contacte al administrador.`, 'error');
                
                // Crear datos de ejemplo temporal para poder seguir trabajando
                productsData = [{
                    id: -1,  // ID negativo para indicar que es un dato temporal
                    code: "TEMP-001",
                    name: "Producto Temporal",
                    category: "Categoría Temporal",
                    description: "Este es un producto de muestra debido a un error de conexión con la API",
                    price: 0,
                    cost: 0,
                    variants: 0,
                    status: "active"
                }];
                
                loadProductsData(productsData);
            });
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

        // Ocultar el toast después de 3 segundos
        setTimeout(() => {
            toast.classList.add('hidden');
        }, 3000);
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

            // Asegurar que price y cost sean números válidos
            const price = parseFloat(item.price) || 0;
            const cost = parseFloat(item.cost) || 0;
            const variants = parseInt(item.variants) || 0;

            // Crear el contenido de la fila
            const rowHTML = `
                <td>${item.code || ''}</td>
                <td>${item.name || ''}</td>
                <td>${item.category || ''}</td>
                <td class="description-cell">${item.description || ''}</td>
                <td>S/. ${price.toFixed(2)}</td>
                <td>S/. ${cost.toFixed(2)}</td>
                <td>${variants}</td>
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
            
            // Imprimir en la consola para depuración
            console.log(`HTML para fila del producto ${item.id}:`, rowHTML);
            
            row.innerHTML = rowHTML;
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
            if (deleteButton) {
                deleteButton.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                    // Obtener el ID desde el data attribute
                    const productId = parseInt(this.getAttribute('data-id'));
                    console.log(`Click en botón eliminar para producto ID: ${productId}`);
                    
                    // Validar que el ID sea un número válido
                    if (isNaN(productId)) {
                        console.error("ID de producto inválido:", this.getAttribute('data-id'));
                        // Mostrar mensaje de error
                        const errorToast = document.getElementById('errorToast');
                        if (errorToast) {
                            errorToast.querySelector('.toast-message').textContent = 'ID de producto inválido';
                            errorToast.classList.remove('hidden');
                            setTimeout(() => errorToast.classList.add('hidden'), 3000);
                        } else {
                            showToast('Error', 'ID de producto inválido', 'error');
                        }
                        return;
                    }
                    
                    // Llamar a la función para eliminar el producto
                    deleteProduct(productId);
                });
            } else {
                console.error('No se encontró el botón de eliminar para el producto:', item.id);
            }
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
            // Modo edición - Obtener el producto del servidor
            fetch(`/api/productos/${productId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error al obtener el producto');
                    }
                    return response.json();
                })
                .then(data => {
                    // Asegurarnos de tener valores por defecto para campos obligatorios
                    selectedProduct = {
                        id: data.id,
                        code: data.codigo || '',
                        name: data.nombre || 'Producto sin nombre',
                        category: data.categoria ? data.categoria.nombre : '',
                        categoryId: data.categoria ? data.categoria.id : null,
                        description: data.descripcion || '',
                        price: data.precio || 0,
                        cost: data.costo || 0,
                        variants: data.variantes ? data.variantes.length : 0,
                        status: data.estado === 'ACTIVO' ? 'active' : 'inactive'
                    };

            productModalTitle.textContent = 'Editar Producto';

            // Llenar el formulario con los datos del producto
            productCode.value = selectedProduct.code;
            productName.value = selectedProduct.name;
                    
                    // Asegurarnos de que la categoría exista en el select
                    const categoryExists = Array.from(productCategory.options).some(option => option.value === selectedProduct.category);
                    if (selectedProduct.category && categoryExists) {
                        productCategory.value = selectedProduct.category;
                    } else if (selectedProduct.category) {
                        // Si la categoría no existe en el select, añadirla
                        const option = document.createElement('option');
                        option.value = selectedProduct.category;
                        option.textContent = selectedProduct.category;
                        productCategory.appendChild(option);
            productCategory.value = selectedProduct.category;
                    }
                    
            productStatus.value = selectedProduct.status;
            productDescription.value = selectedProduct.description;
            productPrice.value = selectedProduct.price;
            productCost.value = selectedProduct.cost;

                    // Cargar variantes si existen
                    if (data.variantes && data.variantes.length > 0) {
                        console.log('Variantes encontradas:', data.variantes.length);
                        variantsContainer.innerHTML = ''; // Limpiar contenedor de variantes
                        data.variantes.forEach(variante => {
                            console.log('Cargando variante:', variante);
                            
                            // Buscar el stock en el inventario si existe
                            let stockActual = 0;
                            
                            // Por defecto, intentamos obtener el stock de alguna manera
                            try {
                                // Buscar en los movimientos si existen
                                if (variante.movimientos && variante.movimientos.length > 0) {
                                    console.log('Movimientos encontrados:', variante.movimientos.length);
                                    // Suponemos que el primer movimiento contiene el stock actual
                                    stockActual = variante.movimientos[0].cantidad || 0;
                                }
                            } catch (err) {
                                console.warn('No se pudo determinar el stock para la variante:', err);
                            }
                            
                            console.log(`Variante ID ${variante.id}: Stock actual = ${stockActual}`);
                            
                            addVariant({
                                id: variante.id, // Guardar el ID para actualizar la variante
                                color: variante.color || '',
                                size: variante.talla || '',
                                stock: stockActual
                            });
                        });
                    } else {
                        console.log('No se encontraron variantes para el producto');
                        variantsContainer.innerHTML = ''; // Limpiar contenedor de variantes
                        addVariant(); // Añadir variante vacía como predeterminado
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast('Error', 'No se pudo cargar el producto. Usando un producto por defecto.', 'error');
                    
                    // Crear un producto por defecto para editar
                    selectedProduct = {
                        id: productId,
                        code: 'ERROR-' + productId,
                        name: 'Producto con error',
                        category: '',
                        description: 'Este producto no pudo ser cargado correctamente',
                        price: 0,
                        cost: 0,
                        variants: 1,
                        status: 'inactive'
                    };
                    
                    productModalTitle.textContent = 'Editar Producto (Datos por defecto)';
                    productCode.value = selectedProduct.code;
                    productName.value = selectedProduct.name;
                    productDescription.value = selectedProduct.description;
                    productPrice.value = selectedProduct.price;
                    productCost.value = selectedProduct.cost;
                    productStatus.value = selectedProduct.status;
                    
                    // Añadir variante vacía
                    addVariant();
                });
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
    function addVariant(data = { color: '', size: '', stock: 0 }) {
        const variantItem = document.createElement('div');
        variantItem.className = 'variant-item';
        
        // Si hay un ID de variante existente, agregarlo como un campo oculto
        const varianteIdHtml = data.id ? `<input type="hidden" class="variant-id" value="${data.id}">` : '';
        // Añadir un campo oculto para el stock
        const stockHtml = `<input type="hidden" class="variant-stock-value" value="${data.stock || 0}">`;
        
        // Añadir una clase para identificar si es una variante existente o nueva
        const variantTypeClass = data.id ? 'existing-variant' : 'new-variant';
        variantItem.classList.add(variantTypeClass);

        variantItem.innerHTML = `
            ${varianteIdHtml}
            ${stockHtml}
            <div class="variant-color">
                <input type="text" class="form-control" placeholder="Color" value="${data.color || ''}">
            </div>
            <div class="variant-size">
                <input type="text" class="form-control" placeholder="Talla" value="${data.size || ''}">
            </div>
            <div class="variant-actions">
                <button type="button" class="remove-variant" title="Quitar variante de la lista">
                    <i class="fas fa-times"></i>
                </button>
                ${data.id ? `<button type="button" class="delete-variant" title="Eliminar variante permanentemente">
                    <i class="fas fa-trash"></i>
                </button>` : ''}
            </div>
        `;

        // Agregar event listener para eliminar variante de la lista
        variantItem.querySelector('.remove-variant').addEventListener('click', function() {
            variantItem.remove();
        });
        
        // Agregar event listener para eliminar variante permanentemente (solo si tiene ID)
        if (data.id) {
            variantItem.querySelector('.delete-variant').addEventListener('click', function() {
                if (confirm(`¿Está seguro de que desea eliminar esta variante (${data.color} - ${data.size})?`)) {
                    deleteVariant(data.id, variantItem);
                }
            });
        }

        variantsContainer.appendChild(variantItem);
    }
    
    // Eliminar una variante permanentemente
    function deleteVariant(varianteId, variantElement) {
        // Mostrar un mensaje de "eliminando..." en el toast
        showToast('Procesando', 'Eliminando variante...', 'success');
        
        fetch(`/api/productos/variante/${varianteId}`, {
            method: 'DELETE'
        })
        .then(response => {
            console.log(`Respuesta del servidor para eliminación de variante:`, response.status, response.statusText);
            
            if (!response.ok) {
                throw new Error(`Error al eliminar la variante: ${response.status} ${response.statusText}`);
            }
            
            // Eliminar la variante de la UI
            variantElement.remove();
            
            showToast('Variante eliminada', 'La variante ha sido eliminada correctamente', 'success');
        })
        .catch(error => {
            console.error('Error al eliminar variante:', error);
            showToast('Error', `No se pudo eliminar la variante: ${error.message}`, 'error');
        });
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
    productModalClose.addEventListener('click', closeProductModal);

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

        // Recopilar datos de variantes
        const variantesData = Array.from(variantItems).map(item => {
            const color = item.querySelector('.variant-color input').value.trim();
            const talla = item.querySelector('.variant-size input').value.trim();
            const stock = parseInt(item.querySelector('.variant-stock-value').value) || 0;
            // Buscar un ID de variante si existe
            const varianteIdInput = item.querySelector('.variant-id');
            const id = varianteIdInput ? parseInt(varianteIdInput.value) || null : null;
            
            if (!color || !talla) {
                showToast('Error', 'Todas las variantes deben tener color y talla', 'error');
                return null;
            }
            
            const variante = {
                color: color,
                talla: talla
            };
            
            // Agregar id solo si existe
            if (id) {
                variante.id = id;
            }
            
            // Establecer un stock inicial fijo de 20 unidades para nuevas variantes,
            // mantener el stock existente para variantes existentes
            const initialStock = !id ? 20 : stock;
            
            // Agregar los datos de movimiento para manejar el stock
            variante.movimientos = [{
                cantidad: initialStock,
                tipo: "ENTRADA",
                motivo: "REPOSICION",
                motivoDetalle: "Stock inicial"
            }];
            
            return variante;
        }).filter(item => item !== null);
        
        // Verificar si hay alguna variante inválida
        if (variantesData.length === 0) {
            showToast('Error', 'Debe proporcionar información válida para al menos una variante', 'error');
            return;
        }

        console.log('Variantes a enviar:', variantesData);

        // Buscar la categoría por nombre
        const selectedCategoryName = productCategory.value;
        let selectedCategory = categoriesData.find(cat => cat.nombre === selectedCategoryName);
        
        // Si no se encuentra la categoría, crear una por defecto
        if (!selectedCategory) {
            selectedCategory = {
                id: null,  // El servidor asignará un ID
                nombre: selectedCategoryName
            };
        }

        // Crear objeto de producto para enviar al servidor
        const productApiData = {
            codigo: productCode.value.trim(),
            nombre: productName.value.trim(),
            categoria: selectedCategory,
            descripcion: productDescription.value.trim(),
            precio: parseFloat(productPrice.value),
            costo: parseFloat(productCost.value),
            estado: productStatus.value === 'active' ? 'ACTIVO' : 'INACTIVO',
            variantes: variantesData
        };

        console.log('Datos del producto a enviar:', productApiData);

        let url = '/api/productos';
        let method = 'POST';

        if (editMode && selectedProduct) {
            // Actualizar producto existente
            url = `/api/productos/${selectedProduct.id}`;
            method = 'PUT';
            productApiData.id = selectedProduct.id;
        }

        // Mostrar indicador de carga
        saveProductButton.disabled = true;
        saveProductButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';

        // Enviar datos al servidor
        fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(productApiData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(editMode ? 'Error al actualizar producto' : 'Error al crear producto');
                }
                return response.json();
            })
            .then(data => {
                console.log('Respuesta del servidor:', data);
                
                // Mostrar confirmación
                showToast(
                    editMode ? 'Producto actualizado' : 'Producto creado',
                    `El producto ${productName.value} ha sido ${editMode ? 'actualizado' : 'creado'} correctamente`
                );

                // Recargar la lista de productos
                fetchProducts();
                
                // Cerrar el modal
                closeProductModal();
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Error', error.message, 'error');
            })
            .finally(() => {
                // Restaurar el botón
                saveProductButton.disabled = false;
                saveProductButton.innerHTML = 'Guardar';
            });
    });

    // Cambiar estado del producto
    function toggleProductStatus(productId, currentStatus) {
        const newStatus = currentStatus === 'active' ? 'INACTIVO' : 'ACTIVO';
        
        fetch(`/api/productos/${productId}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                estado: newStatus
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cambiar el estado del producto');
            }
            return response.json();
        })
        .then(data => {
            const statusText = newStatus === 'ACTIVO' ? 'activado' : 'desactivado';
        showToast('Estado actualizado', `El producto ha sido ${statusText} correctamente`);

            // Recargar la lista de productos
            fetchProducts();
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error', error.message, 'error');
        });
    }

    // Eliminar producto
    function deleteProduct(productId) {
        console.log(`Intentando eliminar producto con ID: ${productId}`);
        
        // Validar que el ID sea válido
        if (!productId || isNaN(productId)) {
            console.error('ID de producto inválido:', productId);
            showToast('Error', 'ID de producto inválido', 'error');
            return;
        }
        
        if (confirm(`¿Está seguro de que desea eliminar el producto con ID ${productId}?`)) {
            console.log(`Confirmación recibida para eliminar producto con ID: ${productId}`);
            
            // Mostrar un mensaje de "eliminando..." en el toast
            showToast('Procesando', 'Eliminando producto...', 'success');
            
            fetch(`/api/productos/${productId}`, {
                method: 'DELETE'
            })
            .then(response => {
                console.log(`Respuesta del servidor para eliminación:`, response.status, response.statusText);
                
                if (!response.ok) {
                    throw new Error(`Error al eliminar el producto: ${response.status} ${response.statusText}`);
                }
                
                showToast('Producto eliminado', 'El producto ha sido eliminado correctamente', 'success');
                
                // Recargar la lista de productos
                fetchProducts();
            })
            .catch(error => {
                console.error('Error al eliminar producto:', error);
                showToast('Error', `No se pudo eliminar el producto: ${error.message}`, 'error');
            });
        } else {
            console.log('Eliminación cancelada por el usuario');
        }
    }

    // Event listeners para el modal de categorías
    newCategoryButton.addEventListener('click', () => openCategoryModal());
    cancelCategoryButton.addEventListener('click', closeCategoryModal);
    categoryModalClose.addEventListener('click', closeCategoryModal);
    saveCategoryButton.addEventListener('click', saveCategory);

    // Añadir botón para gestionar las categorías (listar y editar)
    newCategoryButton.addEventListener('contextmenu', function(e) {
        e.preventDefault(); // Prevenir el menú contextual por defecto
        showCategoriesManagement();
    });

    // Cerrar modal al hacer clic fuera de él
    window.addEventListener('click', function(event) {
        if (event.target === productModal) {
            closeProductModal();
        }
        if (event.target === categoryModal) {
            closeCategoryModal();
        }
    });

    // Función para mostrar listado de categorías para administración
    function showCategoriesManagement() {
        // Crear un modal temporal para mostrar las categorías
        const tempModal = document.createElement('div');
        tempModal.className = 'modal';
        tempModal.style.display = 'block';
        
        let categoriesListHTML = '';
        if (categoriesData.length === 0) {
            categoriesListHTML = '<p>No hay categorías disponibles</p>';
        } else {
            categoriesListHTML = '<table class="data-table" style="width:100%"><thead><tr>' +
                '<th>Nombre</th><th>Descripción</th><th>Estado</th><th>Acciones</th>' +
                '</tr></thead><tbody>';
            
            categoriesData.forEach(cat => {
                const statusClass = cat.estado === 'ACTIVO' || cat.estado === 'Activa' ? 'badge-success' : 'badge-inactive';
                const statusText = cat.estado === 'ACTIVO' || cat.estado === 'Activa' ? 'Activa' : 'Inactiva';
                
                categoriesListHTML += `<tr>
                    <td>${cat.nombre}</td>
                    <td>${cat.descripcion || '-'}</td>
                    <td><span class="badge ${statusClass}">${statusText}</span></td>
                    <td>
                        <button class="edit-category-btn button button-secondary" data-id="${cat.id}">
                            <i class="fas fa-edit"></i> Editar
                        </button>
                    </td>
                </tr>`;
            });
            
            categoriesListHTML += '</tbody></table>';
        }
        
        tempModal.innerHTML = `
            <div class="modal-content" style="max-width: 600px;">
                <div class="modal-header">
                    <h2>Gestión de Categorías</h2>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    ${categoriesListHTML}
                </div>
                <div class="modal-footer">
                    <button class="button button-primary" id="newCategoryFromList">
                        <i class="fas fa-plus"></i> Nueva Categoría
                    </button>
                    <button class="button button-secondary" id="closeCategoryList">
                        Cerrar
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(tempModal);
        
        // Event listeners para el modal temporal
        const closeBtn = tempModal.querySelector('.modal-close');
        const closeCategoryListBtn = tempModal.querySelector('#closeCategoryList');
        const newCategoryBtn = tempModal.querySelector('#newCategoryFromList');
        
        closeBtn.addEventListener('click', () => {
            document.body.removeChild(tempModal);
        });
        
        closeCategoryListBtn.addEventListener('click', () => {
            document.body.removeChild(tempModal);
        });
        
        newCategoryBtn.addEventListener('click', () => {
            document.body.removeChild(tempModal);
            openCategoryModal();
        });
        
        // Event listeners para los botones de editar
        const editButtons = tempModal.querySelectorAll('.edit-category-btn');
        editButtons.forEach(button => {
            button.addEventListener('click', () => {
                const categoryId = parseInt(button.getAttribute('data-id'));
                document.body.removeChild(tempModal);
                openCategoryModal(categoryId, true);
            });
        });
    }

    // Función para abrir el modal de categoría
    function openCategoryModal(categoryId = null, isEdit = false) {
        categoryEditMode = isEdit;
        
        // Resetear el formulario
        categoryName.value = '';
        categoryDescription.value = '';
        categoryStatus.value = 'active';
        
        if (isEdit && categoryId) {
            // Buscar la categoría en la API
            fetch(`/api/categorias/${categoryId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error al obtener la categoría');
                    }
                    return response.json();
                })
                .then(category => {
                    selectedCategory = category;
                    categoryModalTitle.textContent = 'Editar Categoría';
                    
                    // Llenar el formulario con los datos de la categoría
                    categoryName.value = category.nombre;
                    categoryDescription.value = category.descripcion || '';
                    categoryStatus.value = category.estado === 'ACTIVO' || category.estado === 'Activa' ? 'active' : 'inactive';
                    
                    // Mostrar el modal
                    categoryModal.style.display = 'block';
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast('Error', `No se pudo cargar la categoría: ${error.message}`, 'error');
                    
                    // Intentar cargar desde los datos locales
                    const localCategory = categoriesData.find(cat => cat.id === categoryId);
                    if (localCategory) {
                        selectedCategory = localCategory;
                        categoryModalTitle.textContent = 'Editar Categoría (Datos Locales)';
                        
                        // Llenar el formulario con los datos locales
                        categoryName.value = localCategory.nombre;
                        categoryDescription.value = localCategory.descripcion || '';
                        categoryStatus.value = localCategory.estado === 'ACTIVO' || localCategory.estado === 'Activa' ? 'active' : 'inactive';
                        
                        // Mostrar el modal
                        categoryModal.style.display = 'block';
                    } else {
                        showToast('Error', 'No se pudo encontrar la categoría', 'error');
                    }
                });
        } else {
            selectedCategory = null;
            categoryModalTitle.textContent = 'Nueva Categoría';
            
            // Mostrar el modal
            categoryModal.style.display = 'block';
        }
    }

    // Función para cerrar el modal de categoría
    function closeCategoryModal() {
        categoryModal.style.display = 'none';
        selectedCategory = null;
        categoryEditMode = false;
    }

    // Función para guardar una categoría (nueva o editada)
    function saveCategory() {
        // Validar el formulario
        if (!categoryName.value.trim()) {
            showToast('Error', 'Debes ingresar el nombre de la categoría', 'error');
            return;
        }
        
        // Mostrar indicador de carga
        saveCategoryButton.disabled = true;
        saveCategoryButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';
        
        const categoryData = {
            nombre: categoryName.value.trim(),
            descripcion: categoryDescription.value.trim(),
            estado: categoryStatus.value === 'active' ? 'ACTIVO' : 'INACTIVO'
        };
        
        let url = '/api/categorias';
        let method = 'POST';
        
        if (categoryEditMode && selectedCategory) {
            // Actualizar categoría existente
            url = `/api/categorias/${selectedCategory.id}`;
            method = 'PUT';
            categoryData.id = selectedCategory.id;
        }
        
        // Enviar datos al servidor
        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(categoryData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(categoryEditMode ? 'Error al actualizar categoría' : 'Error al crear categoría');
            }
            return response.json();
        })
        .then(data => {
            console.log('Respuesta del servidor:', data);
            
            // Actualizar la categoría en el array local
            if (categoryEditMode && selectedCategory) {
                const index = categoriesData.findIndex(cat => cat.id === selectedCategory.id);
                if (index !== -1) {
                    categoriesData[index] = data;
                }
                
                showToast('Éxito', 'Categoría actualizada correctamente');
            } else {
                // Añadir la nueva categoría al array
                categoriesData.push(data);
                
                showToast('Éxito', 'Categoría creada correctamente');
            }
            
            // Actualizar el select de categorías
            populateCategorySelect();
            
            // Cerrar el modal
            closeCategoryModal();
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error', error.message, 'error');
        })
        .finally(() => {
            // Restaurar el botón
            saveCategoryButton.disabled = false;
            saveCategoryButton.innerHTML = 'Guardar';
        });
    }

    // Inicializar la interfaz
    checkApiHealth();
    
    // Mostrar un mensaje informativo la primera vez
    setTimeout(() => {
        // Verificar si ya se ha mostrado el mensaje
        const tipShown = localStorage.getItem('category_tip_shown');
        if (!tipShown) {
            showToast('Consejo', 'Haz clic derecho en el botón "Nueva Categoría" para gestionar todas las categorías existentes', 'success');
            localStorage.setItem('category_tip_shown', 'true');
        }
    }, 2000);
});