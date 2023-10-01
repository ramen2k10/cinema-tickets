package uk.gov.dwp.uc.pairtest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class AppTest 
{
 
    private static TicketPaymentService paymentService;
    private static SeatReservationService reservationService;
   
    @BeforeClass
	public static void beforeClass() {
       paymentService = mock(TicketPaymentService.class);
       reservationService = mock(SeatReservationService.class);
	}

    @Test
    public void testPurchaseTicketsValid() throws InvalidPurchaseException {
         Long accountId = 123L;
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService, reservationService);

        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);
        verify(paymentService).makePayment(accountId, 70); 
        verify(reservationService).reserveSeat(accountId, 5);
    }

    @Test
    public void testInvalidPurchaseTickets_AdultCountLessThanInfantCount() {
        Long accountId = 123L;
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService, reservationService);

        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        try {
            ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);
        } catch (InvalidPurchaseException e) {
            assertEquals("Invalid ticket purchase request.",e.getMessage());
        }
     }

    @Test
    public void testInvalidPurchaseTickets_exceedMaxPurchase() {
        Long accountId = 123L;
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService, reservationService);

        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 7);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 10);
        try {
            ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);
        } catch (InvalidPurchaseException e) {
            assertEquals("Invalid ticket purchase request.",e.getMessage());
        }
     }

     @Test
    public void testInvalidPurchaseTickets_noAdultType() {
        Long accountId = 123L;
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService, reservationService);

        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 7);
        TicketTypeRequest infantRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 10);
        try {
            ticketService.purchaseTickets(accountId, childRequest, infantRequest);

        } catch (InvalidPurchaseException e) {
            assertEquals("Invalid ticket purchase request.",e.getMessage());
        }
     }
}
