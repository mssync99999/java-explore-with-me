package ru.practicum.ewm.enums;

public enum StatusComment {
    UPDATING, //на редактировании автора
    PENDING, //ожидает ревью админа
    PUBLISHED, //опубликован после проверки админа (PENDING)
    CANCELED, //отклонён автором (ЖЦ прерван)
    BANNED //забанен админом
}
