package cz.asenk.demo.arbes.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import cz.asenk.demo.arbes.billing.model.CallEntry;

/**
 * @author Alois Šenkyřík asenkyrik@monetplus.cz
 * Created: 07.09.2023
 */

@Slf4j
public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator{
    private static final BigDecimal CALL_PRICE_DAY = BigDecimal.valueOf(1.0);
    private static final BigDecimal CALL_PRICE_NIGHT = BigDecimal.valueOf(0.5);
    private static final BigDecimal CALL_PRICE_LONG_CALL = BigDecimal.valueOf(0.2);
    private static final LocalTime DAY_INTERVAL_START = LocalTime.parse("8:00", DateTimeFormatter.ofPattern("H:mm"));
    private static final LocalTime DAY_INTERVAL_END = LocalTime.parse("16:00", DateTimeFormatter.ofPattern("H:mm"));

    @Override
    public BigDecimal calculate(String phoneLog) {
        log.info("Calculating from data -> {}", phoneLog);

        val logData = parseLog(phoneLog);
        val callPriceMap = logData.stream()
                .collect(Collectors.toMap(
                        CallEntry::number,
                        callEntry -> {
                            val list = new ArrayList<BigDecimal>();
                            list.add(TelephoneBillCalculatorImpl.calculateCallPrice(callEntry));
                            return list;
                        },
                        (entry1, entry2) -> {
                            entry1.addAll(entry2);
                            return entry1;
                        }
                ));

        applyPromotion(callPriceMap);

        callPriceMap.forEach((key, value) -> log.info("Entry: {} -> {}", key, value));

        return callPriceMap.values().stream()
                .flatMap(List::stream) // Flatten the list of BigDecimal values
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates call price based on interval operations. The computation first computes the
     * price fee components, and then calculates BigDecimal out of it.
     *
     * If the call is longer than 5 minutes -> set 0.2Kč/min
     * If the call is in interval <8:00, 16:00> (day tax) -> set 1.0Kč/min
     * If the call is out of interval (night tax) -> set 0.5Kč/min
     *
     * The 5 minute fee takes precedence over other conditions.
     *
     * @param entry Entry to compute from
     * @return BigDecimal price value.
     */
    protected static BigDecimal calculateCallPrice(CallEntry entry){
        val durationMinutes = entry.getCalledMinutes();

        val intersectedMinutes = computeIntersectedMinutes(
                entry.callStart().toLocalTime(),
                entry.callEnd().toLocalTime()
        );

        val overhungMinutes = durationMinutes > 5 ? durationMinutes - 5 : 0;
        val nightMinutes = durationMinutes - overhungMinutes - intersectedMinutes;
        val dayMinutes = durationMinutes - overhungMinutes - nightMinutes;

        val priceComponents = Map.of(
                CALL_PRICE_LONG_CALL, overhungMinutes,
                CALL_PRICE_NIGHT, nightMinutes,
                CALL_PRICE_DAY, dayMinutes
        );

        return priceComponents.entrySet().stream()
                .map(it -> it.getKey().multiply(BigDecimal.valueOf(it.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Applices current promos (if I had more time, I would separate it into classes, and apply the promos
     * depending on condition.
     */
    protected static void applyPromotion(Map<String, ArrayList<BigDecimal>> dataMap){
        val maxListSize = dataMap.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        val mostCalledNumbers = dataMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() == maxListSize)
                .map(it -> Long.parseLong(it.getKey()))
                .toList();

        if(mostCalledNumbers.isEmpty())
            return;

        val mostCalledNumber = mostCalledNumbers.size() > 1
                ? mostCalledNumbers.stream().max(Long::compareTo)
                : mostCalledNumbers.get(0);

        val entry = dataMap.entrySet().stream()
                .filter(it -> it.getKey().equals(mostCalledNumber.toString()))
                .findFirst()
                .orElseThrow();

        entry.setValue(new ArrayList<>());
    }

    /**
     * Returns how many minutes are in the <8:00:00, 16:00:00> interval
     * @param start Start time
     * @param end End time
     */
    protected static long computeIntersectedMinutes(LocalTime start, LocalTime end){
        if(start.isAfter(DAY_INTERVAL_END) || end.isBefore(DAY_INTERVAL_START))
            return 0;

        val intersectionStart = start.isAfter(DAY_INTERVAL_START) ? start : DAY_INTERVAL_START;
        val intersectionEnd = end.isBefore(DAY_INTERVAL_END) ? end : DAY_INTERVAL_END;

        return intersectionStart.until(intersectionEnd, java.time.temporal.ChronoUnit.MINUTES);
    }

    /**
     * Parses CSV file in string format into the CallEntry lists.
     */
    private static List<CallEntry> parseLog(String phoneLog){
        if(phoneLog == null)
            return Collections.emptyList();

        val lines = phoneLog.split("\n");
        return Arrays.stream(lines)
                .map(line -> {
                    val parts = line.split(",");
                    return new CallEntry(
                            parts[0],
                            LocalDateTime.parse(parts[1], DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                            LocalDateTime.parse(parts[2], DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                    );
                })
                .collect(Collectors.toList());
    }
}
