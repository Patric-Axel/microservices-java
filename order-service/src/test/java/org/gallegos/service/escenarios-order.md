# Order Service - Escenarios de prueba

## ORD-01 - Crear pedido con stock disponible
DADO: existe el producto solicitado y hay stock suficiente.
CUANDO: se registra una nueva orden.
ENTONCES: la orden se registra como PENDING, se descuenta el stock, cambia a CONFIRMED y ambos estados quedan registrados en el historial.

## ORD-02 - Crear pedido sin stock disponible
DADO: existe el producto solicitado, pero no hay stock suficiente.
CUANDO: se intenta registrar una nueva orden.
ENTONCES: la orden se registra inicialmente como PENDING, cambia a CANCELLED, se registra el cambio en el historial y se informa que no existe stock suficiente.

## ORD-03 - Consultar pedido existente
DADO: existe una orden registrada con el ID solicitado.
CUANDO: se consulta la orden por su ID.
ENTONCES: Order Service devuelve correctamente la orden encontrada.

## ORD-04 - Consultar pedido inexistente
DADO: no existe una orden con el ID solicitado.
CUANDO: se consulta la orden por su ID.
ENTONCES: Order Service rechaza la operación indicando que la orden no existe.

## ORD-05 - Cancelar pedido confirmado
DADO: existe una orden en estado CONFIRMED.
CUANDO: se solicita cancelar la orden.
ENTONCES: se restaura el stock en Inventory, la orden cambia a CANCELLED y el nuevo estado queda registrado en el historial.

## ORD-06 - Rechazar segunda cancelación
DADO: existe una orden que ya se encuentra en estado CANCELLED.
CUANDO: se intenta cancelar nuevamente la misma orden.
ENTONCES: Order Service rechaza la operación, no restaura nuevamente el stock y no registra una segunda cancelación.

## ORD-07 - Consultar historial de pedido
DADO: existe una orden con cambios de estado registrados.
CUANDO: se consulta el historial de la orden.
ENTONCES: Order Service devuelve los estados registrados durante el ciclo de vida de la orden.