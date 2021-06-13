package io.axoniq.labs.chat.query.rooms.messages;

import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.RoomMessagesQuery;
import java.time.Instant;
import java.util.List;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageProjection {

  private final ChatMessageRepository repository;
  private final QueryUpdateEmitter updateEmitter;

  public ChatMessageProjection(ChatMessageRepository repository, QueryUpdateEmitter updateEmitter) {
    this.repository = repository;
    this.updateEmitter = updateEmitter;
  }

  // TODO: Create some event handlers that update this model when necessary.
  @EventHandler
  public void on(MessagePostedEvent event, @Timestamp Instant timestamp) {
    ChatMessage chatMessage = new ChatMessage(event.getParticipant(), event.getRoomId(), event.getMessage(), timestamp.toEpochMilli());
    repository.save(chatMessage);
    // TODO: Emit updates when new message arrive to notify subscription query by modifying the event handler.
    updateEmitter.emit(RoomMessagesQuery.class, roomMessagesQuery -> event.getRoomId().equals(roomMessagesQuery.getRoomId()), chatMessage);
  }

  // TODO: Create the query handler to read data from this model.
  @QueryHandler
  public List<ChatMessage> handle(RoomMessagesQuery query) {
    return repository.findAllByRoomIdOrderByTimestamp(query.getRoomId());
  }
}
