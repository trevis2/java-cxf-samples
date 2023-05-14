package client;

import srl.ibc.ws.donationservice.DonationService;
import srl.ibc.ws.donationservice.DonationServicePortType;

public class WSClient {

	public WSClient() {
	}

	public static void main(String[] args) {
		DonationService service = new DonationService();
		DonationServicePortType port = service.getDonationServicePort();
//        System.out.println(doubleItMessage(port, 10));
//        System.out.println(doubleItMessage(port, 0));
//        System.out.println(doubleItMessage(port, -10));
	}

//    public static String doubleItMessage(DoubleItPortType port, int numToDouble) {
//        int resp = doubleIt(port, numToDouble);
//        return "The number " + numToDouble + " doubled is " + resp;
//    }
//
//    public static int doubleIt(DoubleItPortType port, int numToDouble) {
//        return port.doubleIt(numToDouble);
//    }
}
