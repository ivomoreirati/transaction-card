package br.com.hubfintech.tcp;

import br.com.hubfintech.constants.TransactionResultCode;
import br.com.hubfintech.dto.TransactionRequestDTO;
import br.com.hubfintech.dto.TransactionResponseDTO;
import br.com.hubfintech.services.CardTransactionService;
import br.com.hubfintech.util.JSON;
import br.com.hubfintech.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer{
    
    static public void createServerTCP(int port, CardTransactionService cardTransactionService) {
        
        ServerSocket serverSocket;
        
        try{            
            serverSocket = new ServerSocket();

            serverSocket.setReuseAddress(true);

            InetSocketAddress address = new InetSocketAddress("localhost", port);

            serverSocket.bind(address);

            System.out.println("Create server TCP success: IP: "
                    + address.getAddress().getHostAddress() + " in Port: " + address.getPort());

            while(true)
                processing(serverSocket.accept(), cardTransactionService);
            
        }catch(IOException e){
            
            System.err.println("Error create Server TCP in Port: " + port + e);
        }
    }

    static public void processing(Socket socket, CardTransactionService cardTransactionService) {

        try{
            InputStreamReader instr = new InputStreamReader(socket.getInputStream());

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader bufferedReader = new BufferedReader(instr);

            String request = Util.readLinesBufferedReader(bufferedReader, "}");

            TransactionResponseDTO response_dto;

            if((request != null) && (request.length() > 0)){

                TransactionRequestDTO request_dto =
                        (TransactionRequestDTO) JSON.convertJSON_OBJ(request, TransactionRequestDTO.class);

                if(request_dto != null){

                    response_dto = cardTransactionService.processRequestTransaction(request_dto);

                }else response_dto =
                        CardTransactionService.createTransactionResponseDTOInvalid(null, TransactionResultCode.PROCESSING_ERROR, -1L);

            }else response_dto =
                    CardTransactionService.createTransactionResponseDTOInvalid(null, TransactionResultCode.PROCESSING_ERROR, -1L);

            String response = JSON.convertOBJ_JSON(response_dto);

            out.println(response);

            out.close();
            socket.close();

        }catch(Exception e){

            System.err.println("Client loss conection in Server TCP.");

            e.printStackTrace();
        }
    }
}