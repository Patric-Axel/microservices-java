GW-01 - Petición sin JWT
DADO: una petición a /api/orders sin Authorization.
CUANDO: atraviesa SecurityWebFilterChain.
ENTONCES: responde 401 y no llega al downstream.

GW-02 - Petición con JWT inválido
DADO: una petición con Bearer invalid-token y el decoder lo rechaza.
CUANDO: atraviesa SecurityWebFilterChain.
ENTONCES: responde 401 y no llega al downstream.

GW-03 - Petición con JWT válido
DADO: una petición con un token aceptado por ReactiveJwtDecoder.
CUANDO: atraviesa SecurityWebFilterChain.
ENTONCES: la petición continúa al downstream y obtiene OK.