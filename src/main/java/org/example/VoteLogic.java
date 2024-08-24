package org.example;

import org.example.model.City;

import java.util.*;


public class VoteLogic {
    private Thread thread;
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

    public void run(boolean sendLive) {
        getCities(dataPath);
        voteIncreaseThread(minute, sendLive);
    }

    public void stop(){
        thread.stop();
    }

    private void voteIncreaseThread(int minute, boolean sendLive) {
        thread = new Thread(() -> {
            try {
                int totalIterations = 100;
                int slowIterations = 5;
                int fastIterations = totalIterations - slowIterations;

                long totalTimeMillis = (long) minute * 60000L;


                long slowSleepTime = (long)(totalTimeMillis * 0.6 / slowIterations);


                long fastSleepTime = (long)(totalTimeMillis * 0.4 / fastIterations);

                for (int i = 0; i < totalIterations; ++i) {
                    increaseVotes(i + 1);
                    writeProcess(sendLive);

                    if (i < slowIterations) {
                        Thread.sleep(slowSleepTime);
                    } else {
                        Thread.sleep(fastSleepTime);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        for (int i = 0; i < this.cities.size(); ++i) {
            City targetCity = (City)this.cities.get(i);
            City currentCity = (City)this.increasingCities.get(i);
            HashMap<String, Long> targetVotes = targetCity.getVotes();
            HashMap<String, Long> currentVotes = currentCity.getVotes();

            if (iteration == 100) {
                currentCity.setVotes(targetVotes);
            } else {
                Iterator<String> iterator = targetVotes.keySet().iterator();

                while (iterator.hasNext()) {
                    String party = iterator.next();
                    long targetVote = targetVotes.get(party);
                    long currentVote = currentVotes.get(party);

                    if (currentVote < targetVote) {
                        long difference = targetVote - currentVote;

                        // Increase complexity by adding more variability
                        long maxIncrease = difference / (long)(random.nextInt(15) + 5); // Slightly faster increase

                        // Randomly decide if this party gets a bonus increase
                        if (random.nextDouble() < 0.2) { // 20% chance to get a larger boost
                            maxIncrease *= 3;
                        }

                        long increase = maxIncrease > 0 ? random.nextInt((int) (maxIncrease + 1)) : 0;

                        long newVoteCount = currentVote + increase;
                        currentVotes.put(party, newVoteCount);
                    }
                }

                // Optional: Normalize votes to prevent exceeding target
                normalizeVotes(currentVotes, targetVotes);

                currentCity.setVotes(currentVotes);
            }
        }
    }

    private void normalizeVotes(HashMap<String, Long> currentVotes, HashMap<String, Long> targetVotes) {
        for (String party : currentVotes.keySet()) {
            long currentVote = currentVotes.get(party);
            long targetVote = targetVotes.get(party);

            // Cap votes at the target to prevent exceeding expected values
            if (currentVote > targetVote) {
                currentVotes.put(party, targetVote);
            }
        }
    }



    private void writeProcess(boolean sendLive) {
        outputString.setLength(0);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("Şehir İsmi\\t");
        this.parties.forEach((party) -> {
            sb1.append(party).append("\\t");
        });
        outputString.append(sb1).append("\\n");
        sb1.setLength(0);

        for(int i = 0; i < increasingCities.size(); ++i) {
            City city = increasingCities.get(i);
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

        }));

        if(sendLive){
            writeProcessLive();
        }


    }
    private void writeProcessLive() {
        StringBuilder percentageOutput = new StringBuilder();

        percentageOutput.append("Şehir İsmi\t");
        this.parties.forEach(party -> percentageOutput.append(party).append("\t"));
        percentageOutput.append("\n");

        for (City city : increasingCities) {
            long totalVotes = city.getVotes().values().stream().mapToLong(Long::longValue).sum();
            percentageOutput.append(city.getName()).append("\t");

            for (int j = 0; j < this.parties.size(); ++j) {
                String party = this.parties.get(j);
                long voteCount = city.getVotes().get(party);
                double percentage = totalVotes > 0 ? (double) voteCount / totalVotes * 100 : 0;

                // Son sütunda tab eklemeyelim
                if (j != this.parties.size() - 1) {
                    percentageOutput.append(String.format("%.2f", percentage)).append("\t");
                } else {
                    percentageOutput.append(String.format("%.2f", percentage));
                }
            }
            percentageOutput.append("\n");
        }

        sendDataToLive(percentageOutput.toString());
    }



    private void sendDataToLive(String data) {
        String jsonData = VoteDataConverter.convertToJSON(data);
        HttpRequest.sendDataToLive(jsonData).thenAccept((success -> {
            if (success) {
                System.out.println("Data successfully sent to live.");
            } else {
                System.out.println("Failed to send data to live.");
            }
        }));
    }


}


