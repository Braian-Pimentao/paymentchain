package com.paymentchain.customer.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.internal.filter.ValueNodes;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
class CustomerRestController {

    @Autowired
    private CustomerRepository customerRepository;

    private final WebClient.Builder webClientBuilder;

    public CustomerRestController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    //@Value("${custom.activeprofileName}")
    //private String profile;

    @Autowired
    private Environment env;


    @GetMapping("/check")
    public String check() {
        return "Customer Service is up and running in mode: " + env.getProperty("custom.activeprofileName");
    }

    @GetMapping
    public Iterable<Customer> findAll() {
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity findById( @PathVariable long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return new ResponseEntity<>(customer.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(long id, Customer customer) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            Customer updatedCustomer = existingCustomer.get();
            updatedCustomer.setName(customer.getName());
            updatedCustomer.setLastName(customer.getLastName());
            updatedCustomer.setPhone(customer.getPhone());
            Customer save = customerRepository.save(updatedCustomer);
            return new ResponseEntity<>(save, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        customer.getProducts().forEach(product -> product.setCustomer(customer));
        Customer save = customerRepository.save(customer);
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable long id) {
        customerRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/full")
    public Customer getCustomerFull(@RequestParam(name = "code") String code) {
        Customer customer = customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        products.forEach(product -> {
            String productName = getProductName(product.getProductId());
            product.setProductName(productName);
        });
        List<?> transactions = getIban(customer.getIban());
        customer.setTransactions(transactions);
        return customer;
    }

    private String getProductName(long productId) {
        WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(this.client))
                .baseUrl("http://localhost:8082/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/product"))
                .build();

        return client
                .get()
                .uri("/{id}", productId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block().get("name").asText();
    }


    private List<?> getIban(String iban) {
        WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(this.client))
                .baseUrl("http://localhost:8083/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/transaction"))
                .build();

        return client
                .get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/byAccountIban").
                    queryParam("accountIban",iban).build();
                })
                .retrieve()
                .bodyToFlux(Object.class).collectList()
                .block();
    }
}
