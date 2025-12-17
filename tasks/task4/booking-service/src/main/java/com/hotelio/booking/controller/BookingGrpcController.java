package com.hotelio.booking.controller;

import com.hotelio.booking.service.BookingService;
import com.hotelio.proto.booking.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@RequiredArgsConstructor
@GrpcService
public class BookingGrpcController extends BookingServiceGrpc.BookingServiceImplBase {

    private final BookingService bookingService;

    @Override
    public void createBooking(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
        try {
            log.info("CreateBooking request={}", request);
            responseObserver.onNext(bookingService.createBooking(request));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("CreateBooking request failed, request={}", request, ex);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void listBookings(BookingListRequest request, StreamObserver<BookingListResponse> responseObserver) {
        try {
            log.info("listBookings request={}", request);
            responseObserver.onNext(bookingService.listBookings(request));
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("listBookings failed, request={}", request, ex);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException()
            );
        }
    }
}
