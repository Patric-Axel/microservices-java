GW-04 - Generar X-Trace-Id
DADO: una petición llega al Gateway sin X-Trace-Id, con el valor vacío o en blanco.
CUANDO: atraviesa TraceIdFilter.
ENTONCES: el Gateway genera un X-Trace-Id válido, lo propaga al downstream y lo incluye en la respuesta.

GW-05 - Conservar X-Trace-Id
DADO: una petición llega al Gateway con X-Trace-Id: trace-del-cliente.
CUANDO: atraviesa TraceIdFilter.
ENTONCES: el Gateway conserva el X-Trace-Id recibido, lo propaga al downstream y lo incluye en la respuesta.