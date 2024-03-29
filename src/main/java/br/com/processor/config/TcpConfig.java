package br.com.processor.config;

import br.com.processor.entities.Card;
import br.com.processor.services.CardService;
import br.com.processor.services.CardTransactionService;
import br.com.processor.tcp.TCPServer;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.concurrent.Executors;


/**
 * Class for configuration port socket with threads for receive requests . . .
 *
 * @author Ivo Moreira
 *
 */

@Configuration
@Slf4j
public class TcpConfig {
    
    @Value("${tcp.transaction.port}")
    private int TCP_PORT;
    
    @Autowired
    CardService cardService;
    
    @Autowired
    CardTransactionService cardTransactionService;
    
    @Bean
    InitializingBean inicialize() {

        return () -> {
            
            Card c1 = new Card();
            c1.setCardnumber("1234567890123456");
            c1.setAvailableAmount(new BigDecimal("1000.00"));
            cardService.saveCard(c1);

            Card c2 = new Card();
            c2.setCardnumber("4485617978182589");
            c2.setAvailableAmount(new BigDecimal("1000.00"));
            cardService.saveCard(c2);

            log.info("Initial Data");

            cardService.getAllCards().forEach(card -> {
                log.info(card.toString());
            });

            Executors.newSingleThreadExecutor().execute(() -> {
                try{
                    TCPServer.createServerTCP(TCP_PORT, cardTransactionService);

                }catch(Exception e){
                    log.error("Error create TCP MultiThread. " + e.getMessage());
                }
            });
        };
    }
}
