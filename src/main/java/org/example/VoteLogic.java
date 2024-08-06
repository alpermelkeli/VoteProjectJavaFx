package org.example;

import org.example.model.City;

import java.util.*;


public class VoteLogic {
    private ArrayList<City> cities;
    private ArrayList<City> increasingCities;
    private ArrayList<String> parties;
    private String dataPath;
    private final StringBuilder outputString = new StringBuilder();
    private int minute;
    private String voteType;

    public VoteLogic(String dataPath,  int minute, String voteType) {
        this.dataPath = dataPath;
        this.minute = minute;
        this.voteType = voteType;
    }

    public void run() {
        this.getCities(this.dataPath);
        this.voteIncreaseThread(this.minute);
    }

    private void voteIncreaseThread(int minute) {
        Thread thread = new Thread(() -> {
            try {
                for(int i = 0; i < 100; ++i) {
                    this.increaseVotes(i + 1);
                    this.writeProcess();
                    Thread.sleep((long)minute * 600L);
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        });
        thread.start();
    }

    private void getCities(String dataPath) {
        ArrayList<City> resultCities = new ArrayList();
        ArrayList<City> emptyCities = new ArrayList();
        String[] lines = FileIO.readFile(dataPath, true, true);

        assert lines != null;

        String[] firstLine = lines[0].split("\t");
        ArrayList<String> parties = new ArrayList(Arrays.asList(firstLine).subList(1, firstLine.length));

        for(int i = 1; i < lines.length; ++i) {
            String[] arguments = lines[i].split("\t");
            HashMap<String, Long> votes = new HashMap();
            HashMap<String, Long> emptyVotes = new HashMap();

            for(int j = 1; j < arguments.length; ++j) {
                votes.put(firstLine[j], Long.parseLong(arguments[j]));
                emptyVotes.put(firstLine[j], 0L);
            }

            resultCities.add(new City(arguments[0], votes));
            emptyCities.add(new City(arguments[0], emptyVotes));
        }

        this.cities = resultCities;
        this.increasingCities = emptyCities;
        this.parties = parties;
    }

    private void increaseVotes(int iteration) {
        Random random = new Random();

        for(int i = 0; i < this.cities.size(); ++i) {
            City targetCity = (City)this.cities.get(i);
            City currentCity = (City)this.increasingCities.get(i);
            HashMap<String, Long> targetVotes = targetCity.getVotes();
            HashMap<String, Long> currentVotes = currentCity.getVotes();
            if (iteration == 100) {
                currentCity.setVotes(targetVotes);
            } else {
                Iterator var8 = targetVotes.keySet().iterator();

                while(var8.hasNext()) {
                    String party = (String)var8.next();
                    long targetVote = (Long)targetVotes.get(party);
                    long currentVote = (Long)currentVotes.get(party);
                    if (currentVote < targetVote) {
                        long difference = targetVote - currentVote;
                        long maxIncrease = difference / (long)(100 - iteration + 1);
                        long increase = maxIncrease > 0 ? random.nextInt((int) (maxIncrease + 1)) : 0; // int conversion
                        currentVotes.put(party, currentVote + increase);
                    }
                }

                currentCity.setVotes(currentVotes);
            }
        }

    }

    private void writeProcess() {
        outputString.setLength(0);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("Şehir İsmi\\t");
        this.parties.forEach((party) -> {
            sb1.append(party).append("\\t");
        });
        outputString.append(sb1).append("\\n");
        sb1.setLength(0);

        for(int i = 0; i < this.increasingCities.size(); ++i) {
            City city = (City)this.increasingCities.get(i);
            sb1.append(city.getName()).append("\\t");

            for(int j = 0; j < this.parties.size(); ++j) {
                if (j != this.parties.size() - 1) {
                    sb1.append(city.getVotes().get(this.parties.get(j))).append("\\t");
                } else if (i != this.increasingCities.size() - 1) {
                    sb1.append(city.getVotes().get(this.parties.get(j))).append("\\n");
                } else {
                    sb1.append(city.getVotes().get(this.parties.get(j)));
                }
                outputString.append(sb1);
                sb1.setLength(0);
            }
        }
        HttpRequest.sendDataToServer(voteType,outputString.toString()).thenAccept((aBoolean -> {
            System.out.println(aBoolean);
        }));
    }
}


