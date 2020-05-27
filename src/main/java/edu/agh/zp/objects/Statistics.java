package edu.agh.zp.objects;
import org.springframework.security.core.parameters.P;

public class Statistics {
    enum type {Sejm, Senat, Everyone};
    public long votesCount;
    public long entitledToVote;
    public float frequency;
    public String result;
    public int freq50percent;

    public Chart chart;
    public Statistics(){ ; }

    public Statistics(long votesCount_, long entitledToVote_, Chart chart_, VotingEntity.TypeOfVoting type_) {

        votesCount = votesCount_;
        entitledToVote = entitledToVote_;
        frequency = (float)votesCount_/entitledToVote_ *100;
        chart = chart_;
        freq50percent =  (int) Math.ceil( votesCount / 2.0);
        switch(type_){
            case SEJM:
            case SENAT:
                if(votesCount < (int) Math.ceil( entitledToVote / 2.0) ){
                    result = "Nie wystarczająca ilość głosów.";
                }else {
                    result = "Odrzucono";
                    for( StatisticRecord i : chart.data){
                        if((i.label.equals("Tak") || i.label.equals("Za")) && i.value >= freq50percent) {
                            result = "Przyjęto";
                            break;
                        }
                    }
                }
                break;
            case REFERENDUM:
                result = "Odrzucono";
                for( StatisticRecord record : chart_.data){
                    if (record.value * 2 > votesCount) {
                        result = "Przyjęto";
                    }
                    record.value = record.value/votesCount *100;
                }
                break;
            case PREZYDENT:
                for( StatisticRecord record : chart_.data){
                    if(record.value * 2 > votesCount ){
                        result= record.label;
                    }
                    record.value = record.value/votesCount *100;
                }
                break;
        }

    }
}
