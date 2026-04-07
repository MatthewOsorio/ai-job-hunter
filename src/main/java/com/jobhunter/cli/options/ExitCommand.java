package com.jobhunter.cli.options;

import java.util.List;

import com.jobhunter.cli.Main;

public class ExitCommand extends MenuItem {
  private List<String> goodByesMessages = List.of("See you later, alligator!",
      "In a while, crocodile!", "Gotta bounce, pounce, out the door, dinosaur!",
      "Catch you on the flip side!", "Adios, amigos!", "Farewell, my friend!",
      "Until we meet again, take care!", "Goodbye, and good luck with your job hunting!",
      "May the job offers be ever in your favor!", "Gotta go, buffalo!", "Hang loose, moose!",
      "Take it easy, cheesy!", "Peace out, rainbow trout!", "Hasta la vista, baby!");

  public ExitCommand() {
    super("Exit");
  }

  @Override
  public void run() {
    int randomIndex = (int) (Math.random() * goodByesMessages.size());
    String randomGoodbye = goodByesMessages.get(randomIndex);
    Main.console.generalCat(randomGoodbye);
    System.exit(0);
  }
}
