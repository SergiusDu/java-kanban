package task.dto;

public sealed interface TaskCreationDTO
    permits EpicTaskCreationDTO, RegularTaskCreationDTO, SubTaskCreationDTO {
  String title();

  String description();
}
