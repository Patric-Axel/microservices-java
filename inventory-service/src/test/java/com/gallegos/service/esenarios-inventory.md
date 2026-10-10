# Inventory Service - Escenarios de prueba

## INV-01 - Crear producto
DADO: un producto válido para registrar.
CUANDO: se solicita crear el producto.
ENTONCES: Inventory guarda el producto con su fecha y hora de registro y devuelve el producto creado.

## INV-02 - Consultar producto existente
DADO: existe un producto registrado con el ID solicitado.
CUANDO: se consulta el producto por su ID.
ENTONCES: Inventory devuelve correctamente el producto encontrado.

## INV-03 - Consultar producto inexistente
DADO: no existe un producto con el ID solicitado.
CUANDO: se consulta el producto por su ID.
ENTONCES: Inventory rechaza la operación indicando que el producto no existe.

## INV-04 - Descontar stock suficiente
DADO: existe stock suficiente para descontar la cantidad solicitada.
CUANDO: se solicita descontar el stock del producto.
ENTONCES: Inventory realiza el descuento y confirma que el stock fue actualizado correctamente.

## INV-05 - Stock insuficiente
DADO: el producto no tiene stock suficiente para la cantidad solicitada.
CUANDO: se intenta descontar el stock.
ENTONCES: Inventory no realiza el descuento e informa que la actualización no pudo realizarse.

## INV-06 - Restaurar stock
DADO: se deben devolver unidades al stock de un producto.
CUANDO: se solicita restaurar la cantidad correspondiente.
ENTONCES: Inventory incrementa el stock y confirma si la restauración fue realizada correctamente.

## INV-07 - Rechazar cantidad inválida
DADO: se intenta descontar o restaurar stock utilizando una cantidad nula, cero o negativa.
CUANDO: se solicita modificar el stock.
ENTONCES: Inventory rechaza la operación y no realiza ninguna modificación sobre el stock.