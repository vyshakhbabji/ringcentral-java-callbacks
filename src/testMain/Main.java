package testMain;


import http.ApiCallback;
import http.ApiException;
import http.ApiResponse;
import http.Client;
import platform.Platform;

import java.io.IOException;

public class Main {
	
	Platform p ;
	
	
	Main(Platform p){
		this.p = p;
	}
	
	
	public void getExtension(Platform p){
		
		System.out.println("Inside p");
		
		p.get(
                "/restapi/v1.0/account/~/extension", null, null, new ApiCallback() {

                    @Override
                    public void onResponse(ApiResponse response) {
                        try {
                            System.out.println(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(ApiException e) {
                    	System.out.println(e.getMessage());
                    }
                });
	}

    public static void main(String[] args) throws IOException {

        Client c = new Client();
        
        //final Main main= new Main(p);
        
        final Platform p = new Platform(c, "E0_nOAfbR7GkteYbDv93oA",
                "UelNnk-1QYK0rHyvjJJ9yQx3Yl6vj3RvGmb0G2SH6ePw",
                Platform.Server.SANDBOX);

        final Main main= new Main(p);
        
        p.login("15856234138", "101", "P@ssw0rd", new ApiCallback() {
            @Override
            public void onResponse(ApiResponse response) {
                System.out.println(response.code());
                main.getExtension(p);
            }

            @Override
            public void onFailure(ApiException e) {
                // TODO Auto-generated method stub
            	
            }
        });

        
       
        
//        p.get(
//                "/restapi/v1.0/account/~/extension", null, null, new ApiCallback() {
//
//                    @Override
//                    public void onResponse(ApiResponse response) {
//                        try {
//                            System.out.println(response.body().string());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(ApiException e) {
//                    	System.out.println(e.getMessage());
//                    }
//                });
//   
//        
//        

    }
}