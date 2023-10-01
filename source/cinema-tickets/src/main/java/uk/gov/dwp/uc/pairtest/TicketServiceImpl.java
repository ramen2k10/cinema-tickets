package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import java.util.List;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAX_TICKETS_PER_TRANSACTION = 20;
    private static final int ADULT_TICKET_PRICE = 20;
    private static final int CHILD_TICKET_PRICE = 10;

    private TicketPaymentService paymentService = null;
    private SeatReservationService reservationService = null;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if(accountId <= 0){
            throw new InvalidPurchaseException("Invalid Account/Insufficient funds"); 
        }

        int infantHeadCounts = getHeadCount(TicketTypeRequest.Type.INFANT, ticketTypeRequests);
        int childHeadCounts = getHeadCount(TicketTypeRequest.Type.CHILD, ticketTypeRequests);
        int adultHeadCounts = getHeadCount(TicketTypeRequest.Type.ADULT, ticketTypeRequests);
        
        boolean isValidPurchase = (adultHeadCounts > 0) // At least one adult required
                                    && (infantHeadCounts <= adultHeadCounts) // Infants must not exceed adults
                                    && (infantHeadCounts + childHeadCounts + adultHeadCounts <= MAX_TICKETS_PER_TRANSACTION);// Total tickets within limit

        if(isValidPurchase){
            int totalSeatResarvationCount = childHeadCounts + adultHeadCounts;
            int totalAmountToPay = (childHeadCounts * CHILD_TICKET_PRICE) + (adultHeadCounts * ADULT_TICKET_PRICE);
             // Make a payment request && seat reservation request
            if(paymentService != null && reservationService != null){
                paymentService.makePayment(accountId, totalAmountToPay);
                reservationService.reserveSeat(accountId, totalSeatResarvationCount);
            }else{
                throw new InvalidPurchaseException("Payment service or Reservation service is NULL");
            }
        }else {
            throw new InvalidPurchaseException("Invalid ticket purchase request.");
        }
    }

    private int getHeadCount(Type ticketType, TicketTypeRequest... ticketTypeRequests) {
        if(ticketTypeRequests == null){
            throw new InvalidPurchaseException("TicketTypeRequest argument lists is NULL ");
        }
        List<TicketTypeRequest> requestList = Arrays.asList(ticketTypeRequests);
        return requestList.stream()
                .filter(request -> request.getTicketType() == ticketType && request.getNoOfTickets() >= 0) // Filter non-negative counts
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

}
