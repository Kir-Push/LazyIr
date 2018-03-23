package com.example.buhalo.lazyir.Devices;



/**
 * Created by buhalo on 03.04.17.
 */

public class IrCommand {
    private String producer;
    private String command_name;
    private String command;
    private String owner_id;
    private String type;

    public static String ir = "ir";

    public IrCommand() {
    }

    public IrCommand(String producer, String type,String command_name, String command, String owner_id) {
        this.command_name = command_name;
        this.command = command;
        this.owner_id = owner_id;
        this.type = type;
        this.producer = producer;
    }

    public String getCommand_name() {
        return command_name;
    }

    public void setCommand_name(String command_name) {
        this.command_name = command_name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IrCommand irCommand = (IrCommand) o;

        if (producer != null ? !producer.equals(irCommand.producer) : irCommand.producer != null)
            return false;
        if (command_name != null ? !command_name.equals(irCommand.command_name) : irCommand.command_name != null)
            return false;
        if (command != null ? !command.equals(irCommand.command) : irCommand.command != null)
            return false;
        if (owner_id != null ? !owner_id.equals(irCommand.owner_id) : irCommand.owner_id != null)
            return false;
        return type != null ? type.equals(irCommand.type) : irCommand.type == null;

    }

    @Override
    public int hashCode() {
        int result = producer != null ? producer.hashCode() : 0;
        result = 31 * result + (command_name != null ? command_name.hashCode() : 0);
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + (owner_id != null ? owner_id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IrCommand{" +
                "producer='" + producer + '\'' +
                ", command_name='" + command_name + '\'' +
                ", command='" + command + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
