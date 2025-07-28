import org.json.JSONObject;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecretRecovery {

    public static void main(String[] args) {
        String testCase1 = "{\"keys\":{\"n\":4,\"k\":3},\"1\":{\"base\":\"10\",\"value\":\"4\"},\"2\":{\"base\":\"2\",\"value\":\"111\"},\"3\":{\"base\":\"10\",\"value\":\"12\"},\"6\":{\"base\":\"4\",\"value\":\"213\"}}";
        String testCase2 = "{\"keys\":{\"n\":10,\"k\":7},\"1\":{\"base\":\"6\",\"value\":\"13444211440455345511\"},\"2\":{\"base\":\"15\",\"value\":\"aed7015a346d63\"},\"3\":{\"base\":\"15\",\"value\":\"6aeeb69631c227c\"},\"4\":{\"base\":\"16\",\"value\":\"e1b5e05623d881f\"},\"5\":{\"base\":\"8\",\"value\":\"316034514573652620673\"},\"6\":{\"base\":\"3\",\"value\":\"2122212201122002221120200210011020220200\"},\"7\":{\"base\":\"3\",\"value\":\"20120221122211000100210021102001201112121\"},\"8\":{\"base\":\"6\",\"value\":\"20220554335330240002224253\"},\"9\":{\"base\":\"12\",\"value\":\"45153788322a1255483\"},\"10\":{\"base\":\"7\",\"value\":\"1101613130313526312514143\"}}";

        BigInteger secret1 = recoverSecret(testCase1);
        BigInteger secret2 = recoverSecret(testCase2);

        System.out.println("Secret for test case 1: " + secret1);
        System.out.println("Secret for test case 2: " + secret2);
    }

    private static BigInteger recoverSecret(String jsonInput) {
        JSONObject obj = new JSONObject(jsonInput);
        JSONObject keysObj = obj.getJSONObject("keys");
        int n = keysObj.getInt("n");
        int k = keysObj.getInt("k");

        List<BigInteger> xList = new ArrayList<>();
        List<BigInteger> yList = new ArrayList<>();
        Set<BigInteger> seenX = new HashSet<>();

        for (String key : obj.keySet()) {
            if (key.equals("keys")) continue;
            JSONObject shareObj = obj.getJSONObject(key);
            String baseStr = shareObj.getString("base");
            String valueStr = shareObj.getString("value");
            int base = Integer.parseInt(baseStr);
            BigInteger x = new BigInteger(key);
            BigInteger y = new BigInteger(valueStr, base);

            if (seenX.contains(x)) {
                continue;
            }
            seenX.add(x);
            xList.add(x);
            yList.add(y);
        }

        if (xList.size() < k) {
            System.out.println("Insufficient valid shares");
            return BigInteger.valueOf(-1);
        }

        List<List<Integer>> combinations = generateCombinations(xList.size(), k);
        List<BigInteger> candidateSecrets = new ArrayList<>();

        for (List<Integer> comb : combinations) {
            List<BigInteger> xs = new ArrayList<>();
            List<BigInteger> ys = new ArrayList<>();
            for (int index : comb) {
                xs.add(xList.get(index));
                ys.add(yList.get(index));
            }

            BigInteger totalNum = BigInteger.ZERO;
            BigInteger totalDen = BigInteger.ONE;
            boolean validCombination = true;

            for (int i = 0; i < xs.size(); i++) {
                BigInteger num_i = ys.get(i);
                BigInteger den_i = BigInteger.ONE;

                for (int j = 0; j < xs.size(); j++) {
                    if (i == j) continue;
                    num_i = num_i.multiply(xs.get(j).negate());
                    den_i = den_i.multiply(xs.get(i).subtract(xs.get(j)));
                }

                if (den_i.equals(BigInteger.ZERO)) {
                    validCombination = false;
                    break;
                }

                BigInteger newNum = totalNum.multiply(den_i).add(num_i.multiply(totalDen));
                BigInteger newDen = totalDen.multiply(den_i);

                if (newDen.equals(BigInteger.ZERO)) {
                    validCombination = false;
                    break;
                }

                BigInteger gcd = newNum.gcd(newDen);
                if (!gcd.equals(BigInteger.ZERO)) {
                    newNum = newNum.divide(gcd);
                    newDen = newDen.divide(gcd);
                }

                if (newDen.compareTo(BigInteger.ZERO) < 0) {
                    newNum = newNum.negate();
                    newDen = newDen.negate();
                }

                totalNum = newNum;
                totalDen = newDen;
            }

            if (!validCombination) {
                continue;
            }

            if (totalDen.compareTo(BigInteger.ZERO) < 0) {
                totalNum = totalNum.negate();
                totalDen = totalDen.negate();
            }

            if (totalDen.equals(BigInteger.ONE)) {
                candidateSecrets.add(totalNum);
            }
        }

        if (candidateSecrets.isEmpty()) {
            System.out.println("No valid secret found");
            return BigInteger.valueOf(-1);
        }

        Map<BigInteger, Integer> frequencyMap = new HashMap<>();
        for (BigInteger cand : candidateSecrets) {
            frequencyMap.put(cand, frequencyMap.getOrDefault(cand, 0) + 1);
        }

        int maxCount = 0;
        List<BigInteger> bestCandidates = new ArrayList<>();
        for (Map.Entry<BigInteger, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestCandidates.clear();
                bestCandidates.add(entry.getKey());
            } else if (entry.getValue() == maxCount) {
                bestCandidates.add(entry.getKey());
            }
        }

        if (bestCandidates.isEmpty()) {
            System.out.println("No valid secret found");
            return BigInteger.valueOf(-1);
        } else {
            Collections.sort(bestCandidates);
            return bestCandidates.get(0);
        }
    }

    private static List<List<Integer>> generateCombinations(int n, int k) {
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        generateCombinationsHelper(0, n, k, current, result);
        return result;
    }

    private static void generateCombinationsHelper(int start, int n, int k, List<Integer> current, List<List<Integer>> result) {
        if (k == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < n; i++) {
            current.add(i);
            generateCombinationsHelper(i + 1, n, k - 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}