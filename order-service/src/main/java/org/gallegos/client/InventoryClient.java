package org.gallegos.client;


import org.gallegos.dto.ProductDTO;
import org.gallegos.exception.InventoryUnavailableException;
import org.gallegos.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Component
public class InventoryClient {
	
	private final WebClient webClient;
	
	public InventoryClient(WebClient webClient) {
        this.webClient = webClient;
    }
	
	
	public ProductDTO getProductById(Integer id) {

	    try {

	        return webClient
	                .get()
	                .uri("/{id}", id)
	                .retrieve()
	                .onStatus(
	                	    status -> status == HttpStatus.NOT_FOUND,
	                	    response -> Mono.error(
	                	        new ProductNotFoundException(
	                	            "El producto con ID " + id + " no existe"
	                	        )
	                	    )
	                	)
	                .bodyToMono(ProductDTO.class)
	                .block();

	    } catch (WebClientRequestException ex) {

	        throw new InventoryUnavailableException(
	                "El servicio de inventario no se encuentra disponible"
	        );
	    }
	}
	
	public Boolean checkStock(Integer idProduct, Integer quantity) {

	    return webClient
	            .get()
	            .uri(uriBuilder -> uriBuilder
	                    .path("/{id}/availability")
	                    .queryParam("quantity", quantity)
	                    .build(idProduct))
	            .retrieve()
	            .bodyToMono(Boolean.class)
	            .block();
	}
	
	public Boolean decreaseStock(Integer idProduct, Integer quantity) {

	    return webClient
	            .patch()
	            .uri(uriBuilder -> uriBuilder
	                    .path("/{id}/stock")
	                    .queryParam("quantity", quantity)
	                    .build(idProduct))
	            .retrieve()
	            .bodyToMono(Boolean.class)
	            .block();
	}
	
	public Boolean increaseStock(Integer idProduct, Integer quantity) {

	    return webClient
	            .patch()
	            .uri(uriBuilder -> uriBuilder
	                    .path("/{id}/stock/restore")
	                    .queryParam("quantity", quantity)
	                    .build(idProduct))
	            .retrieve()
	            .bodyToMono(Boolean.class)
	            .block();
	}
	
	

}
