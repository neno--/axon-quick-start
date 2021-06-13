package io.axoniq.labs.chat.query.rooms.participants;

import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.RoomParticipantsQuery;
import java.util.List;
import java.util.stream.Collectors;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RoomParticipantsProjection {

  private final RoomParticipantsRepository repository;

  public RoomParticipantsProjection(RoomParticipantsRepository repository) {
    this.repository = repository;
  }

  // TODO: Create some event handlers that update this model when necessary.
  @EventHandler
  public void on(ParticipantJoinedRoomEvent event) {
    repository.save(new RoomParticipant(event.getRoomId(), event.getParticipant()));
  }

  @EventHandler
  public void on(ParticipantLeftRoomEvent event) {
    repository.deleteByParticipantAndRoomId(event.getParticipant(), event.getRoomId());
  }

  // TODO: Create the query handler to read data from this model.
  @QueryHandler
  public List<String> handle(RoomParticipantsQuery query) {
    return repository.findRoomParticipantsByRoomId(query.getRoomId()).stream()
        .map(RoomParticipant::getParticipant)
        .sorted()
        .collect(Collectors.toList());
  }
}
