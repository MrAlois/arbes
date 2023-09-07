package cz.asenk.demo.arbes.billing;

import java.math.BigDecimal;

public interface TelephoneBillCalculator {

  BigDecimal calculate (String phoneLog);

}
