package de.codeinfection.quickwango.BasicApi.Controller.Chat;

import de.codeinfection.quickwango.ApiBukkit.ApiSerializable;

/**
 *
 * @author CodeInfection
 */
public final class ChatMessage implements ApiSerializable
{
    public final String author;
    public final String message;
    public final boolean api;

    public ChatMessage(final String author, final String message, final boolean api)
    {
        this.author = author;
        this.message = message;
        this.api = api;
    }

    public Object serialize()
    {
        return new String[] {this.author, this.message, (this.api ? "api" : "user")};
    }
}
