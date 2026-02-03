package com.jets.chat.client.ui.viewmodel;

import com.jets.chat.common.dto.MessageDTO;
import com.jets.chat.common.dto.UserDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

public class MainViewModel {
    private final ObservableList<UserDTO> contacts = FXCollections.observableArrayList();
    private final ObservableList<MessageDTO> messages = FXCollections.observableArrayList();

    public MainViewModel() {
        contacts.add(new UserDTO("Sarah Chen", "Online", "Hey! Are you free?", "#0084FF"));
        contacts.add(new UserDTO("Marcus Johnson", "Away", "I sent the files", "#FF9500"));

        messages.add(new MessageDTO("Hey! How are you doing?", LocalDateTime.now(), false));
        messages.add(new MessageDTO("I'm doing great! Just finished the presentation.",
                LocalDateTime.now(), true));
        messages.add(new MessageDTO("That's awesome! Did you include the data?",
                LocalDateTime.now(), false));
    }

    public ObservableList<UserDTO> getContacts() {
        return contacts;
    }

    public ObservableList<MessageDTO> getMessages() {
        return messages;
    }
}
