levantar

1.	En la ruta de la carpeta raíz del proyecto , ejecutar en PS: 
docker compose up –build
2.	Verificar con: docker compose ps
3.	Abrir Postman importar la colección que se encuentra en el proyecto en: Postman / Order System.postman_collection.
4.	Ir a Setting > Proxy > Desactivar Use system Proxy.
5.	Probar las requests sin Access Token.
6.	Una vez importado obtener el ACCESS TOKEN en:
Order System > Authorization > Get new acces Token.
7.	La ruta del api Gateway es http://localhost:8082/api/ es la única puerta de entrada.
8.	Credenciales para keycloak y al dar click en Get new acces token
Username = user
Password = 123456789

9.	Flujo recomendado
·	Crear producto.
·	Listar productos.
·	Consultar producto por ID.
·	Registrar pedido.
·	Listar pedidos.
·	Consultar pedido por ID.
·	Consultar historial.
·	Cancelar pedido.
·	Consultar nuevamente el historial.
·	Ejecutar los casos de error de la colección.
Flujo principal:
API Gateway → Order Service → Inventory Service`
El pedido inicia en PENDING. Con stock suficiente pasa a CONFIRMED;
sin stock suficiente pasa a CANCELLED. Los cambios se registran en el
historial.

10.	El mismo X-Trace-Id se propaga por:
API Gateway → Order Service → Inventory Service
Puede verificarse con:
docker compose logs api-gateway
docker compose logs order-service
docker compose logs inventory-service
También puede enviarse manualmente.
