package com.onsite.edgexdecivewrapper.model;

import lombok.Data;

import java.util.List;

@Data
public class MapperConfig {
    private boolean nameInTopic;
    private boolean resourceInTopic;
    private List<Integer> namePositions;
    private List<Integer> resourcePositions;
    private String fieldName;
    private String resourceName;

    @Override
    public String toString(){
        return "nameInTopic: "+this.nameInTopic+"\n"+
                "resourceInTopic: "+this.resourceInTopic+"\n"+
                "namePositions: "+this.namePositions.toString()+"\n"+
                "resourcePositions: "+this.resourcePositions.toString()+"\n"+
                "fieldName: "+this.fieldName+"\n"+
                "resourceName: " + this.resourceName;
    }
}
