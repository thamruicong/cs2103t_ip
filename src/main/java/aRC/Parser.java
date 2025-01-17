package arc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Encapsulates a Parser that makes sense of the user input
 */
public class Parser {

    private Storage storage;
    private TaskList tasks;
    private NoteList notes;

    /**
     * Constructor for Parser
     * @param storage Storage object that handles loading and saving of data
     * @param tasks TaskList object that stores all user tasks
     */
    public Parser(Storage storage, TaskList tasks, NoteList notes) {
        this.storage = storage;
        this.tasks = tasks;
        this.notes = notes;
    }

    /**
     * Evaluates user's input according to a set of fixed commands
     * @param input User input represented by a String
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parse(String input) throws DukeException {
        String[] command = input.split(" ");
        String mainCommand = command[0];
        String[] commandArgs = Arrays.copyOfRange(command, 1, command.length);

        switch (mainCommand) {
        case "list":
            return this.parseList(commandArgs);
        case "mark":
            return this.parseMark(commandArgs);
        case "unmark":
            return this.parseUnmark(commandArgs);
        case "todo":
            return this.parseTodo(commandArgs);
        case "deadline":
            return this.parseDeadline(commandArgs);
        case "event":
            return this.parseEvent(commandArgs);
        case "delete":
            return this.parseDelete(commandArgs);
        case "find":
            return this.parseFind(commandArgs);
        case "bye":
            return this.parseBye(commandArgs);
        case "notes":
            return this.parseListNotes(commandArgs);
        case "note":
            return this.parseAddNote(commandArgs);
        case "deletenote":
            return this.parseDeleteNote(commandArgs);
        default:
            throw new InvalidCommandException();
        }
    }

    /**
     * Parses the list command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws InvalidArgumentException If additional arguments are entered
     */
    public String parseList(String[] commandArgs) throws InvalidArgumentException {
        if (commandArgs.length != 0) {
            throw new InvalidArgumentException();
        }

        return this.tasks.listTasks("");
    }

    /**
     * Parses the mark command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseMark(String[] commandArgs) throws DukeException {
        if (commandArgs.length != 1) {
            throw new InvalidArgumentException();
        }

        int index = this.validateIndex(commandArgs[0], Duke.Datatype.TASK) - 1;
        String output = this.tasks.getTask(index).mark();

        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Parses the unmark command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseUnmark(String[] commandArgs) throws DukeException {
        if (commandArgs.length != 1) {
            throw new InvalidArgumentException();
        }

        int index = this.validateIndex(commandArgs[0], Duke.Datatype.TASK) - 1;
        String output = this.tasks.getTask(index).unmark();

        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Parses the todo command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseTodo(String[] commandArgs) throws DukeException {
        String title = String.join(" ", commandArgs);

        if (title == "") {
            throw new EmptyTitleException();
        }

        assert title != "";
        String output = this.tasks.addTask(new Todo(title, false));

        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Parses the deadline command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseDeadline(String[] commandArgs) throws DukeException {
        if (!Arrays.asList(commandArgs).contains("/by")) {
            throw new InvalidArgumentException();
        }

        int indexOfBy = Arrays.asList(commandArgs).indexOf("/by");
        String title = String.join(" ", Arrays.copyOfRange(commandArgs, 0, indexOfBy));
        String deadline = String.join(" ",
                Arrays.copyOfRange(commandArgs, indexOfBy + 1, commandArgs.length));

        if (title == "") {
            throw new EmptyTitleException();
        } else if (deadline == "") {
            throw new InvalidArgumentException();
        }

        assert title != "";
        assert deadline != "";

        LocalDate ld = this.validateDateTime(deadline);
        String output = this.tasks.addTask(new Deadline(title, false, ld));

        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Parses the event command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseEvent(String[] commandArgs) throws DukeException {
        if (!Arrays.asList(commandArgs).contains("/at")) {
            throw new InvalidArgumentException();
        }

        int indexOfBy = Arrays.asList(commandArgs).indexOf("/at");
        String title = String.join(" ", Arrays.copyOfRange(commandArgs, 0, indexOfBy));
        String time = String.join(" ",
                Arrays.copyOfRange(commandArgs, indexOfBy + 1, commandArgs.length));

        if (title == "") {
            throw new EmptyTitleException();
        } else if (time == "") {
            throw new InvalidArgumentException();
        }

        assert title != "";
        assert time != "";

        String output = this.tasks.addTask(new Event(title, false, time));

        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Parses the delete command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseDelete(String[] commandArgs) throws DukeException {
        if (commandArgs.length != 1) {
            throw new InvalidArgumentException();
        }

        int index = this.validateIndex(commandArgs[0], Duke.Datatype.TASK) - 1;
        String output = this.tasks.deleteTask(index);

        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Parses the find command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws InvalidArgumentException If no arguments are entered
     */
    public String parseFind(String[] commandArgs) throws InvalidArgumentException {
        if (commandArgs.length == 0) {
            throw new InvalidArgumentException();
        }

        String keyword = String.join(" ", commandArgs);
        assert keyword != "";

        return this.tasks.listTasks(keyword);
    }

    /**
     * Parses the bye command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws InvalidArgumentException If additional arguments are entered
     */
    public String parseBye(String[] commandArgs) throws InvalidArgumentException {
        if (commandArgs.length != 0) {
            throw new InvalidArgumentException();
        }

        return UI.sayBye();
    }

    /**
     * Parses the notes command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws InvalidArgumentException If additional arguments are entered
     */
    public String parseListNotes(String[] commandArgs) throws InvalidArgumentException {
        if (commandArgs.length != 0) {
            throw new InvalidArgumentException();
        }

        return this.notes.listNotes();
    }

    /**
     * Parses the note command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseAddNote(String[] commandArgs) throws DukeException {
        if (commandArgs.length == 0) {
            throw new InvalidArgumentException();
        }

        String title;
        String description;

        if (!Arrays.asList(commandArgs).contains("/desc")) {
            title = String.join(" ", commandArgs);
            description = "";
        } else {
            int indexOfDesc = Arrays.asList(commandArgs).indexOf("/desc");
            title = String.join(" ", Arrays.copyOfRange(commandArgs, 0, indexOfDesc));
            description = String.join(" ",
                    Arrays.copyOfRange(commandArgs, indexOfDesc + 1, commandArgs.length));
        }

        String output = this.notes.addNote(new Note(title, description));

        this.storage.save(this.notes);
        return output;
    }

    /**
     * Parses the delete-note command
     * @param commandArgs Array of Strings representing command arguments
     * @return An output message
     * @throws DukeException Throws a aRC.DukeException specific to this program
     */
    public String parseDeleteNote(String[] commandArgs) throws DukeException {
        if (commandArgs.length != 1) {
            throw new InvalidArgumentException();
        }

        int index = this.validateIndex(commandArgs[0], Duke.Datatype.NOTE) - 1;
        String output = this.notes.deleteNote(index);

        this.storage.save(this.notes);
        return output;
    }

    /**
     * Validates a given index String
     * @param index The index String
     * @return The integer index if it is valid
     * @throws InvalidArgumentException If the index String is invalid
     */
    public int validateIndex(String index, Duke.Datatype type) throws InvalidArgumentException {
        int intIndex;

        try {
            intIndex = Integer.parseInt(index);
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException();
        }

        if (type == Duke.Datatype.TASK && (intIndex <= 0 || intIndex > this.tasks.numTasks())) {
            throw new InvalidArgumentException();
        }

        if (type == Duke.Datatype.NOTE && (intIndex <= 0 || intIndex > this.notes.numNotes())) {
            throw new InvalidArgumentException();
        }

        return intIndex;
    }

    /**
     * Validates a given datetime String
     * @param dateTimeString The datetime String
     * @return The dateTime as a LocalDate object
     * @throws DukeException If the datetime String is invalid
     */
    public LocalDate validateDateTime(String dateTimeString) throws DukeException {
        LocalDate dateTime;

        try {
            dateTime = LocalDate.parse(dateTimeString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new DukeException("Date format should be dd/mm/yyyy");
        }

        return dateTime;
    }
}
