package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.CreateRoomCommand;
import io.axoniq.labs.chat.coreapi.DummyCommand;
import io.axoniq.labs.chat.coreapi.DummyEvent;
import io.axoniq.labs.chat.coreapi.JoinRoomCommand;
import io.axoniq.labs.chat.coreapi.LeaveRoomCommand;
import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.PostMessageCommand;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.annotation.MessageIdentifier;
import org.axonframework.messaging.annotation.MetaDataValue;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CommandHandlerInterceptor;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aggregate
public class ChatRoom {

  private static final Logger logger = LoggerFactory.getLogger(ChatRoom.class);

  @AggregateIdentifier
  private String roomId;
  private Set<String> participants;

  public ChatRoom() {
  }

  @CommandHandler
  public ChatRoom(CreateRoomCommand command, @MessageIdentifier String messageId) {
    logger.info("CreateRoomCommand message id is: {}", messageId);
    AggregateLifecycle.apply(new RoomCreatedEvent(command.getRoomId(), command.getName()));
  }

  @CommandHandler
  public void handle(JoinRoomCommand command, MetaData metaData, @MetaDataValue("timestamp") Instant now) {
    String participan = command.getParticipant();
    if (participants.contains(participan)) {
      return;
    }
    AggregateLifecycle.apply(new ParticipantJoinedRoomEvent(roomId, command.getParticipant()));
  }

  @CommandHandler
  public void handle(LeaveRoomCommand command, UnitOfWork unitOfWork) {
    String participant = command.getParticipant();
    logger.info("Is work phase started? {}", unitOfWork.phase().isStarted());
    if (participants.contains(participant)) {
      AggregateLifecycle.apply(new ParticipantLeftRoomEvent(roomId, command.getParticipant()));
    }
  }

  @CommandHandler
  public void handle(PostMessageCommand command) {
    String participan = command.getParticipant();
    if (!participants.contains(participan)) {
      throw new IllegalStateException(String.format("Participant [%s] needs to be a part of the room to post messages", participan));
    }
    AggregateLifecycle.apply(new MessagePostedEvent(roomId, command.getParticipant(), command.getMessage()));
  }

  @CommandHandler
  public void handle(DummyCommand  command, @MessageIdentifier String messageId) {
    logger.info("DummyCommand message id is: {}", messageId);

    AggregateLifecycle.apply(new DummyEvent(roomId));
  }

  @EventSourcingHandler
  public void on(RoomCreatedEvent event, @MessageIdentifier String messageId) {
    logger.info("(Sourcing handler) RoomCreatedEvent message id is: {}", messageId);
    roomId = event.getRoomId();
    participants = new HashSet<>();
  }

  @EventSourcingHandler
  public void on(ParticipantJoinedRoomEvent event) {
    participants.add(event.getParticipant());
    //AggregateLifecycle.markDeleted();
  }

  @EventSourcingHandler
  public void on(DummyEvent event, @MessageIdentifier String messageId) {
    logger.info("(sourcing handler) DummyEvent message id is: {}", messageId);
    if (AggregateLifecycle.isLive()) {
      logger.info("Sourcing DummyEvent for NEW event");
    } else {
      logger.info("Sourcing DummyEvent for old event");
    }
  }

  @EventSourcingHandler
  public void on(ParticipantLeftRoomEvent event) {
    participants.remove(event.getParticipant());
  }

  @CommandHandlerInterceptor
  public void intercept(JoinRoomCommand command, InterceptorChain interceptorChain) throws Exception {
    logger.info("In interceptor before: {}", interceptorChain);
    //Object result = interceptorChain.proceed();
    //logger.info("In interceptor after: {}", result.getClass());
  }
}
