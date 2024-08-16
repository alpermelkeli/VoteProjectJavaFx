package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteDataConverter {

    public static String convertToJSON(String data) {
        // \\t ve \\n kaçış karakterlerini \t ve \n olarak değiştir
        data = data.replace("\\t", "\t").replace("\\n", "\n");

        String[] lines = data.split("\n");
        List<Map<String, Object>> citiesData = new ArrayList<>();

        // İlk satır başlıkları alır.
        String[] headers = lines[0].split("\t");

        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split("\t");
            Map<String, Object> cityData = new HashMap<>();
            cityData.put("city", values[0]);

            for (int j = 1; j < values.length; j++) {
                String percentageValue = values[j]; // Yüzdelik değerler string olacak
                cityData.put(headers[j], percentageValue + "%"); // Yüzdelik işareti ekle
            }

            citiesData.add(cityData);
        }

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n\"cities\":[");

        if (!citiesData.isEmpty()) {
            for (Map<String, Object> city : citiesData) {
                jsonBuilder.append("\n{");
                for (Map.Entry<String, Object> entry : city.entrySet()) {
                    jsonBuilder.append("\"").append(entry.getKey()).append("\":");
                    jsonBuilder.append("\"").append(entry.getValue()).append("\"");
                    jsonBuilder.append(",");
                }
                jsonBuilder.setLength(jsonBuilder.length() - 1);
                jsonBuilder.append("},");
            }
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("\n]\n}");

        return jsonBuilder.toString();
    }
}
