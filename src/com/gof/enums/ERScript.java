package com.gof.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ERScript {
	 SM_INTERPOL("AA")
	,SW_ALPHA(" SW.alpha.find <- function ( int, obs.mat, ufr, ufr.t                       #산출 필수 데이터"
			+ "			                            ,min_alpha = 0.001, max_alpha = 1      #알파의 구간을 지정 --> 기본 0.001, 1"
			+ "			                            ,tol = 0.0001, llp = max(obs.mat)      #수렴정도, LLP 지정"
			+ "			                            ,bse_dt = NA, real_date_tf = F         #실제 날자 기준 사용시 필요데이터"
			+ "			                            ,type = \"cont\"                       #입력 금리 타입(복리, 연속복리)"
			+ "							 )"
			+ "	{"
			+ "	  #ufr을 연속 복리로 변환"
			+ "	  ufrc <- Int.disc.to.cont(ufr)"
			+ "	  #금리 데이터가 연속복리가 아닌 경우 연속복리로 변환"
			+ "	  if (type == \"cont\" || type == \"CONT\") { "
			+ "		rate <- int"
			+ "	  } else {rate <- Int.disc.to.cont(int)  }"
			+ "	  #실제 일자를 사용여부 체크 후 만기 변환"
			+ "	  if(real_date_tf == T & is.na(bse_dt) == F){"
			+ "    #실제 일자를 기준으로 계산하는 경우"
			+ "    time.tmp <- e.date(bse_dt, max(obs.mat)*12)"
			+ "    #월말의 날자를 계산하여 365로 나눈 일할 방식"
			+ "    mat <- time.tmp$DIFF_DATE[which(time.tmp$MONTH_SEQ%in%(12*obs.mat) == T)]/365"
			+ "	  } else {"
			+ "	    #실제 일자가 아닌 obs.mat을 그대로 사용하는 경우"
			+ "	    mat <- obs.mat"
			+ "	  }"
			+ "	  #Initializing"
			+ "	  temp.alpha <- round2((min_alpha + max_alpha)/2,6)"
			+ "	  extend <- round2((max_alpha - min_alpha)/2,6)"
			+ "	  weight <- Weight.sw(mat, mat, ufrc, temp.alpha)"
			+ "	  inv.weight <- solve(weight)"
			+ "	  loss <- Sw.loss(rate, mat, ufrc)"
			+ "	  zeta <- inv.weight%*%loss"
			+ "	  sinh <- sinh(mat*temp.alpha)"
			+ "	  q.mat <- diag(exp(-ufrc*mat))"
			+ "	  kappa <- (1+temp.alpha*mat%*%q.mat%*%zeta) / t(sinh)%*%q.mat%*%zeta"
			+ "	  approaching <- sign(temp.alpha/(1-kappa*exp(temp.alpha*llp))) #1회만 산출"
			+ "	  direction <- sign(approaching*temp.alpha/(1-kappa*exp(temp.alpha*ufr.t))-tol)"
			+ "	  loop_idx <- 20   #알파를 찾기 위해서 몇 번이나 루프문을 돌릴 것인지 결정"
			+ "	  #Storage 생성"
			+ "	  alpha.test <- rep(NA, loop_idx) ; alpha.test[1] <- temp.alpha"
			+ "	  kappa.test <- rep(NA, loop_idx) ; kappa.test[1] <- kappa"
			+ "	  dir.test   <- rep(NA, loop_idx) ; dir.test[1]   <- direction"
			+ "	  extend.test   <- rep(NA, loop_idx) ; extend.test[1] <- extend"
			+ "	  min.alpha.test <-rep(NA, loop_idx) ; min.alpha.test[1] <- min_alpha"
			+ "	  max.alpha.test <-rep(NA, loop_idx) ; max.alpha.test[1] <- max_alpha"
			+ "	  #Optimizing"
			+ "	  for (i in 2: loop_idx)  {"
			+ "	    if(direction == -1) { max_alpha <- max_alpha - extend}"
			+ "		else if (direction == 1) {min_alpha <- min_alpha + extend}"
			+ "     temp.alpha <- round2((min_alpha + max_alpha)/2,6)"
			+ "	    extend <- round2((max_alpha - min_alpha)/2,6)"
			+ "	    weight <- Weight.sw(mat, mat, ufrc, temp.alpha)"
			+ "	    inv.weight <- solve(weight)"
			+ "	    zeta <- inv.weight%*%loss"
			+ "	    sinh <- sinh(mat*temp.alpha)"
			+ "	    q.mat <- diag(exp(-ufrc*mat))"
			+ "	    kappa <- (1+temp.alpha*mat%*%q.mat%*%zeta) / t(sinh)%*%q.mat%*%zeta"
			+ "	    direction <- sign(approaching*temp.alpha/(1-kappa*exp(temp.alpha*ufr.t))-tol)"
			+ "	    alpha.test[i] <- temp.alpha"
			+ "	    kappa.test[i] <- kappa"
			+ "	    dir.test[i]   <- direction"
			+ "	    extend.test[i]   <- extend"
			+ "	    min.alpha.test[i] <- min_alpha"
			+ "	    max.alpha.test[i] <- max_alpha"
			+ "	  }"
			+ "	  alpha <- round2((min.alpha.test[loop_idx] + max.alpha.test[loop_idx])/2,6)"
			+ "	  return(alpha)"
			+ "	}"
			)
	,HULL_WHITE_1F("AA")
	,
	
	;
	
	private static Logger logger = LoggerFactory.getLogger(ERScript.class);
	private String script;
	private ERScript(String script) {
		this.script = script;
	}
	public String getScript() {
		return script;
	}
	
}
