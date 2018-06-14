package gtbbackend.command;

import gtbbackend.session.UserSessionManager;
import gtbbackend.user.UserId;
import gtbbackend.practice.PracticeParseResult;
import gtbbackend.practice.PracticeParser;
import gtbbackend.session.Session;

import java.util.*;

/**
 * Routet unbearbeitete Bot-Messages zu entsprechenden Commands
 */
public class MessageHandler
{
    private final UserSessionManager sessionManager;
    private Collection<BotCommand> commands;

    public MessageHandler(UserSessionManager sessionManager){
        this.sessionManager = sessionManager;
        commands = new ArrayList<>();
    }
    /**
     * Versucht eine Nachricht eines Bots als Command zu intertretieren.
     */
    public String handleMessage(UserId userId, String botMessage)
    {
        String response = "";
        LinkedList<String> tokens = new LinkedList<>(Arrays.asList(botMessage.split(" ")));

        if(!tokens.isEmpty())
        {
            String prefix = tokens.getFirst();
            System.out.println("Command-Prefix: " + prefix);
            Optional<BotCommand> botCommand = commands.stream().filter(command ->
                    prefix.equalsIgnoreCase(command.getCommandPrefix())).findFirst();
            if(botCommand.isPresent())
            {
                tokens.removeFirst();
                response = botCommand.get().executeCommand(userId, tokens);
            }
            else
            {
                Optional<Session> activeSession = sessionManager.getActiveSession(userId);
                if(activeSession.isPresent())
                {
                    PracticeParseResult parseResult = new PracticeParser().parseToPractice(tokens);
                    if(parseResult.hasResult())
                    {
                        //sessionManager.addPractice(activeSession.get().getSessionId(), parseResult.getMaybePractice().get());
                    }
                    else
                    {
                        response = parseResult.getMaybeError().get();
                    }
                }
                else
                {
                    response = "Keine aktive Sitzung. Mit /start eine neue Sitzung beginnen.";
                }
            }
        }
        return response;

    }

    public void registerCommand(BotCommand command){
        if(commands.stream().anyMatch(actual -> actual.getCommandPrefix().equalsIgnoreCase(command.getCommandPrefix())))
        {
            throw new IllegalArgumentException("prefix " + command.getCommandPrefix() + " already registered");
        }
        commands.add(command);
    }
}
