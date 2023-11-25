/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.lib.net.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.polsl.screensharing.lib.net.StreamingSignalState;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFrameDetails {
    private double aspectRatio;
    private StreamingSignalState streamingSignalState;

    @Override
    public String toString() {
        return "{" +
            "aspectRatio=" + aspectRatio +
            ", streamingSignalState=" + streamingSignalState +
            '}';
    }
}
