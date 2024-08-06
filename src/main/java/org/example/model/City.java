package org.example.model;
import java.util.HashMap;

public class City {
    private String name;
    private HashMap<String, Long> votes;

    public City(String name, HashMap<String, Long> votes) {
        this.name = name;
        this.votes = votes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Long> getVotes() {
        return votes;
    }

    public void setVotes(HashMap<String, Long> votes) {
        this.votes = votes;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.name+"\n");
        this.votes.forEach((party,vote)->{
            sb.append("parti: "+party + " oy sayısı: "+ vote);
        });
        return sb.toString();
    }
}

