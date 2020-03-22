package com.epam.brn.csv

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.csv.converter.impl.firstSeries.ExerciseCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.GroupCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.SeriesCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.TaskCsv1SeriesConverter
import com.epam.brn.csv.converter.impl.secondSeries.Exercise2SeriesConverter
import com.epam.brn.csv.firstSeries.TaskCSVParser1SeriesService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedExerciseCSVParserService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedGroupCSVParserService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedSeriesCSVParserService
import com.epam.brn.csv.secondSeries.CSVParser2SeriesService
import com.epam.brn.exception.FileFormatException
import com.epam.brn.job.CsvUtils
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.service.InitialDataLoader
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class CsvUploadService(
    private val csvParser: CsvMappingIteratorParser,
    private val groupRepository: ExerciseGroupRepository,
    private val seriesRepository: SeriesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val taskRepository: TaskRepository
) {

    @Value("\${brn.dataFormatNumLines}")
    val dataFormatLinesCount = 5

    @Autowired
    lateinit var groupConverter: GroupCsvConverter

    @Autowired
    lateinit var exerciseConverter: ExerciseCsvConverter

    @Autowired
    lateinit var seriesConverter: SeriesCsvConverter

    @Autowired
    lateinit var task1SeriesConverter: TaskCsv1SeriesConverter

    @Autowired
    lateinit var exercise2SeriesConverter: Exercise2SeriesConverter

    @Autowired
    lateinit var seriesService: SeriesService

    @Autowired
    lateinit var resourceService: ResourceService

    fun loadGroups(inputStream: InputStream): MutableIterable<ExerciseGroup> {
        val groups = csvParser
            .parse(inputStream, groupConverter, CommaSeparatedGroupCSVParserService())

        return groupRepository.saveAll(groups)
    }

    fun loadSeries(inputStream: InputStream): MutableIterable<Series> {
        val series = csvParser
            .parse(inputStream, seriesConverter, CommaSeparatedSeriesCSVParserService())

        return seriesRepository.saveAll(series)
    }

    @Throws(FileFormatException::class)
    fun loadExercises(seriesId: Long, file: MultipartFile): List<Any> {

        if (!isFileContentTypeCsv(file.contentType ?: StringUtils.EMPTY))
            throw FileFormatException()

        return when (seriesId.toInt()) {
            1 -> loadTasksFor1Series(file.inputStream)
            2 -> loadTasksFor2Series(file.inputStream)
            else -> throw IllegalArgumentException("There no one strategy yet for seriesId = $seriesId")
        }
    }

    private fun isFileContentTypeCsv(contentType: String): Boolean = CsvUtils.isFileContentTypeCsv(contentType)

    fun loadExercises(inputStream: InputStream): MutableList<Exercise> {
        val exercises = csvParser
            .parse(inputStream, exerciseConverter, CommaSeparatedExerciseCSVParserService())

        return exerciseRepository.saveAll(exercises)
    }

    @Throws(FileFormatException::class)
    fun loadTasks(file: File): List<Task> = loadTasksFor1Series(file.inputStream())

    fun loadTasksFor1Series(inputStream: InputStream): MutableList<Task> {
        val tasks = csvParser
            .parse(inputStream, task1SeriesConverter, TaskCSVParser1SeriesService())

        return taskRepository.saveAll(tasks)
    }

    fun loadTasksFor2Series(inputStream: InputStream): MutableList<Exercise> {
        val exercises = csvParser
            .parse(inputStream, exercise2SeriesConverter, CSVParser2SeriesService())

        return exerciseRepository.saveAll(exercises)
    }

    fun loadExercisesFor3Series(inputStream: InputStream): MutableList<Exercise> {
        // todo: get data from file for 3 series
        val exercises = createExercises()

        return exerciseRepository.saveAll(exercises)
    }

    fun createExercises(): List<Exercise> {
        val task = createTask()
        val exercise = createExercise()

        task.exercise = exercise
        exercise.addTask(task)

        seriesService.findSeriesWithExercisesForId(3L).exercises.add(exercise)

        return listOf(exercise)
    }

    private fun createExercise(): Exercise {
        return Exercise(
            series = seriesService.findSeriesForId(3L),
            name = "Распознование предложений из 2 слов",
            description = "Распознование предложений из 2 слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseTypeEnum.SENTENCE.toString(),
            level = 1
        )
    }

    private fun createTask(): Task {
        val resource1 = Resource(
            word = "девочкаTest",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/девочка.mp3",
            pictureFileUrl = "pictures/withWord/девочка.jpg"
        )
        val resource2 = Resource(
            word = "дедушкаTest",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/дедушка.mp3",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
        )
        val resource3 = Resource(
            word = "бабушкаTest",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/бабушка.mp3",
            pictureFileUrl = "pictures/withWord/бабушка.jpg"
        )
        val resource4 = Resource(
            word = "бросаетTest",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/бросает.mp3",
            pictureFileUrl = "pictures/withWord/бросает.jpg"
        )
        val resource5 = Resource(
            word = "читаетTest",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/читает.mp3",
            pictureFileUrl = "pictures/withWord/читает.jpg"
        )
        val resource6 = Resource(
            word = "рисуетTest",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/рисует.mp3",
            pictureFileUrl = "pictures/withWord/рисует.jpg"
        )

        val answerOptions =
            mutableSetOf(resource1, resource2, resource3, resource4, resource5, resource6)

        val correctAnswer = Resource(
            word = "девочка рисует",
            wordType = WordTypeEnum.SENTENCE.toString(),
            audioFileUrl = "series3/девочка_рисует.mp3"
        )

        resourceService.saveAll(answerOptions)
        resourceService.save(correctAnswer)

        return Task(
            serialNumber = 2,
            answerOptions = answerOptions,
            correctAnswer = correctAnswer,
            answerParts = mutableMapOf(1 to resource1, 2 to resource6)
        )
    }

    fun getSampleStringForSeriesFile(seriesId: Long): String {
        return readFormatSampleLines(InitialDataLoader.getInputStreamFromSeriesInitFile(seriesId))
    }

    private fun readFormatSampleLines(inputStream: InputStream): String {
        return getLinesFrom(inputStream, dataFormatLinesCount).joinToString("\n")
    }

    private fun getLinesFrom(inputStream: InputStream, linesCount: Int): MutableList<String> {
        inputStream.use {
            val strings = mutableListOf<String>()

            val reader = LineNumberReader(InputStreamReader(inputStream))
            while (reader.lineNumber < linesCount) {
                reader.readLine().let { strings.add(it) }
            }
            return strings
        }
    }
}
