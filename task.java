import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class ShamirSecretRecovery {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) {
            sb.append(sc.nextLine());
        }
        sc.close();

        JSONObject obj = new JSONObject(sb.toString());
        int n = obj.getInt("n");
        int k = obj.getInt("k");
        JSONArray keys = obj.getJSONArray("keys");

        List<BigInteger> xList = new ArrayList<>();
        List<BigInteger> yList = new ArrayList<>();
        Set<BigInteger> seenX = new HashSet<>();

        for (int i = 0; i < keys.length(); i++) {
            String keyStr = keys.getString(i);
            List<BigInteger> nums = extractBigIntegers(keyStr);
            if (nums.size() < 2) {
                continue;
            }
            BigInteger x = nums.get(0);
            BigInteger y = nums.get(1);
            if (seenX.contains(x)) {
                continue;
            }
            seenX.add(x);
            xList.add(x);
            yList.add(y);
        }

        if (xList.size() < k) {
            System.out.println("Insufficient valid shares");
            return;
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

            try {
                BigInteger numTotal = BigInteger.ZERO;
                BigInteger denTotal = BigInteger.ONE;

                for (int i = 0; i < xs.size(); i++) {
                    BigInteger numerator = BigInteger.ONE;
                    BigInteger denominator = BigInteger.ONE;
                    for (int j = 0; j < xs.size(); j++) {
                        if (i == j) continue;
                        numerator = numerator.multiply(xs.get(j).negate());
                        denominator = denominator.multiply(xs.get(i).subtract(xs.get(j)));
                    }
                    if (denominator.equals(BigInteger.ZERO)) {
                        throw new ArithmeticException("Division by zero");
                    }
                    BigInteger termNum = ys.get(i).multiply(numerator);
                    BigInteger newNum = numTotal.multiply(denominator).add(termNum.multiply(denTotal));
                    BigInteger newDen = denTotal.multiply(denominator);

                    BigInteger gcd = newNum.gcd(newDen);
                    if (!gcd.equals(BigInteger.ZERO)) {
                        newNum = newNum.divide(gcd);
                        newDen = newDen.divide(gcd);
                    }

                    if (newDen.signum() < 0) {
                        newDen = newDen.negate();
                        newNum = newNum.negate();
                    }

                    numTotal = newNum;
                    denTotal = newDen;
                }

                if (denTotal.equals(BigInteger.ZERO)) {
                    continue;
                }

                BigInteger gcdFinal = numTotal.gcd(denTotal);
                if (!gcdFinal.equals(BigInteger.ZERO)) {
                    numTotal = numTotal.divide(gcdFinal);
                    denTotal = denTotal.divide(gcdFinal);
                }

                if (denTotal.signum() < 0) {
                    denTotal = denTotal.negate();
                    numTotal = numTotal.negate();
                }

                if (denTotal.equals(BigInteger.ONE)) {
                    candidateSecrets.add(numTotal);
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (candidateSecrets.isEmpty()) {
            System.out.println("No valid secret found");
            return;
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
        } else {
            Collections.sort(bestCandidates);
            System.out.println(bestCandidates.get(0));
        }
    }

    private static List<BigInteger> extractBigIntegers(String s) {
        List<BigInteger> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("-?\\d+");
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String token = matcher.group();
            try {
                BigInteger num = new BigInteger(token);
                list.add(num);
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return list;
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