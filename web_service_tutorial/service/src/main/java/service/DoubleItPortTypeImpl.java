package service;

import javax.jws.WebService;

import srl.ibc.ws.donationservice.DonationServicePortType;
import srl.ibc.ws.donationservice.common.GetDonationRequest;
import srl.ibc.ws.donationservice.common.GetDonationResponse;

@WebService(targetNamespace = "http://www.ibc.srl/ws/donationservice", portName = "DonationServicePort", serviceName = "DonationService", endpointInterface = "srl.ibc.ws.donationservice.DonationServicePortType")
public class DoubleItPortTypeImpl implements DonationServicePortType {

	public int doubleIt(int numberToDouble) {
		return numberToDouble * 2;
	}

	@Override
	public GetDonationResponse getDonation(GetDonationRequest getDonationRequest) {
		// TODO Auto-generated method stub
		return null;
	}
}
