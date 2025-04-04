package com.autumnia.openai.texttospeech.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class TtsController {
    private final  OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @PostMapping("/upload")                                                                                                     //     tts.txt
    public ResponseEntity<StreamingResponseBody> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        // 업로드된 파일의 텍스트 내용 읽기
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .speed(1.1f)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(content, options);

        // 리액티브 스트림 생성(실시간 오디오 스트리밍) - LLM(text)--->Auido(mp3)
        Flux<SpeechResponse> responseStream = openAiAudioSpeechModel.stream(speechPrompt);

        // StreamingResponseBody로 변환하여 클라이언트로 스트림 반환
        StreamingResponseBody stream = outputStream ->
            responseStream
                .toStream()
                .forEach(
                        speechResponse -> writeToOutput(outputStream, speechResponse)
                );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg") // MP3 파일로 설정
                .body(stream); // byte[]/ byte[], byte[]
    }

    private void writeToOutput(OutputStream outputStream, SpeechResponse speechResponse) {
        try {
            // 데이터를 출력 스트림에 작성
            outputStream.write(speechResponse.getResult().getOutput());  // byte[]
            outputStream.flush(); // 즉시 전송
        }
        catch (IOException e) {
            throw new RuntimeException("Error writing audio data to output stream", e);
        }
    }
}
