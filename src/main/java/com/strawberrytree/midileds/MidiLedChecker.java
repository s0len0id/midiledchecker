package com.strawberrytree.midileds;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MidiLedChecker {

    private static final String DEVICE_NAME = "K-Board";
//    private static final String DEVICE_NAME = "APC Key 25";
    private static final String DESCRIPTION = "External MIDI Port";
//    private static final String DESCRIPTION = "No details available";


    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {

        MidiLedChecker midiLedChecker = new MidiLedChecker();

        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        System.out.println("MidiDeviceInfos:" + midiLedChecker.toString(infos));
        List<MidiDevice> midiDevices = midiLedChecker.getDevices(infos);
        printDevices(midiDevices);


        MidiDevice midiDevice = midiLedChecker.getDesiredDevice(infos);
        midiDevice.open();
        midiLedChecker.blink(midiDevice.getReceiver());
        midiLedChecker.lightloop(midiDevice.getReceiver());
        midiDevice.close();


    }

    private void lightloop(Receiver receiver) throws InvalidMidiDataException, InterruptedException {
        ShortMessage myMsg = new ShortMessage();
        int velocity;
        int channel=0;
        long slaap = 10;
        for(velocity=0; velocity<25; velocity++) {
        for (int note = 24; note < 52; note++) {

            System.out.println("n,v = " + note + "," + velocity);
            myMsg.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
            receiver.send(myMsg, -1L);
            Thread.sleep(slaap);}

        }
    }

    private void blink(Receiver receiver) throws InvalidMidiDataException, InterruptedException {
        ShortMessage myMsg = new ShortMessage();
        final int BRIGHTNESS_MIN=0;
        final int BRIGHTNESS_MAX=16;
        final int LED_NUMBER_MIN=0;
        final int LED_NUMBER_MAX=24;
        int channel=0;
        long slaap = 5;

        for(int times=0; times<30; times++) {
            for (int note = LED_NUMBER_MIN; note <= LED_NUMBER_MAX; note++) {
                myMsg.setMessage(ShortMessage.NOTE_ON, channel, note, BRIGHTNESS_MAX);
                receiver.send(myMsg, -1L);
                Thread.sleep(slaap);}
            for (int note = LED_NUMBER_MIN; note <= LED_NUMBER_MAX; note++) {
                myMsg.setMessage(ShortMessage.NOTE_ON, channel, note, BRIGHTNESS_MIN);
                receiver.send(myMsg, -1L);
                Thread.sleep(slaap);}
        }
    }

    private static boolean isDesiredDevice(MidiDevice.Info info) {
        return (info.getName().contains(DEVICE_NAME)) && info.getDescription().contains(DESCRIPTION);
    }

    private MidiDevice getDesiredDevice(MidiDevice.Info[] infos) throws MidiUnavailableException {
        List<MidiDevice.Info> infosDesired = Arrays.stream(infos)
                .filter(MidiLedChecker::isDesiredDevice)
                .collect(Collectors.toList());
        if (infosDesired.size()!=1) {
            throw new RuntimeException("Require 1 connected device, but detected: " + infosDesired.size());
        }
        return MidiSystem.getMidiDevice(infosDesired.get(0));
    }

    private String toString(MidiDevice.Info[] infos) {
        StringBuilder pretty = new StringBuilder();
        for(MidiDevice.Info info : infos){
            pretty.append("\n\nname = ").append(info.getName())
                    .append("\ndesc = ").append(info.getDescription())
                    .append("\nvendor = ").append(info.getVendor())
                    .append("\nversion = ").append(info.getVersion());
        }
        return pretty.toString();
    }

    // broken?
    private static void printDevices(List<MidiDevice> devices) throws MidiUnavailableException {

        for (MidiDevice device : devices) {
            device.open();
            System.out.println("\ndevice = " + device);
            final List<Receiver> receivers = device.getReceivers();
            receivers.forEach(receiver -> System.out.println("receiver = " + receiver));
            final List<Transmitter> transmitters = device.getTransmitters();
            transmitters.forEach(transmitter -> System.out.println("transmitter = " + transmitter));
            device.close();
        }
    }

    private List<MidiDevice> getDevices(MidiDevice.Info[] infos) throws MidiUnavailableException {
        List<MidiDevice> midiDevices = new ArrayList<>();
        for(MidiDevice.Info info : infos) {
            midiDevices.add(MidiSystem.getMidiDevice(info));
        }
        return midiDevices;
    }

}
