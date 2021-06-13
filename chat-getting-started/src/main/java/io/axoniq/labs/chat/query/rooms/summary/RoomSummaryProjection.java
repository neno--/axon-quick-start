package io.axoniq.labs.chat.query.rooms.summary;

import io.axoniq.labs.chat.coreapi.AllRoomsQuery;
import io.axoniq.labs.chat.coreapi.DummyCommand;
import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import java.util.List;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.annotation.MessageIdentifier;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RoomSummaryProjection {

  private static final Logger logger = LoggerFactory.getLogger(RoomSummaryProjection.class);

  private final RoomSummaryRepository roomSummaryRepository;
  private final CommandGateway commandGateway;

  public RoomSummaryProjection(RoomSummaryRepository roomSummaryRepository,
      @SuppressWarnings("SpringJavaAutowiringInspection") CommandGateway commandGateway) {
    this.roomSummaryRepository = roomSummaryRepository;
    this.commandGateway = commandGateway;
  }

  // TODO: Create some event handlers that update this model when necessary.
  @EventHandler
  public void on(RoomCreatedEvent event, @MessageIdentifier String messageId) {
    logger.info("(projection handler) RoomCreatedEvent message id is: {}", messageId);
    commandGateway.send(new DummyCommand(event.getRoomId()));

    roomSummaryRepository.save(new RoomSummary(event.getRoomId(), event.getName()));
  }

  public void on(ParticipantJoinedRoomEvent event) {
    roomSummaryRepository.findById(event.getRoomId()).ifPresent(RoomSummary::addParticipant);
  }

  public void on(ParticipantLeftRoomEvent event) {
    roomSummaryRepository.findById(event.getRoomId()).ifPresent(RoomSummary::removeParticipant);
  }

  // TODO: Create the query handler to read data from this model.
  @QueryHandler
  public List<RoomSummary> handle(AllRoomsQuery query) {
    return roomSummaryRepository.findAll();
  }
}
