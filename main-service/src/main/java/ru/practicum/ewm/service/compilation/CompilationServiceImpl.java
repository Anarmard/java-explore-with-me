package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.errorHandler.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    // public
    // получение подборок событий
    @Override
    public List<CompilationDto> getCompilationList(Boolean pinned, Pageable pageable) {
        Page<Compilation> compilationPage = compilationRepository.findAllByPinnedOrderByIdDesc(pinned, pageable);
        List<Compilation> compilationList = compilationPage.getContent();
        return compilationMapper.toCompilationDtoList(compilationList);
    }

    // получение подборки событие по его id
    @Override
    public CompilationDto getCompilation(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation does not exist with id" + compilationId));
        return compilationMapper.toCompilationDto(compilation);
    }

    // admin
    // добавление новой подборки
    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        // если в подборке уже есть какие-то события, то их нужно сохранить
        if (newCompilationDto.getEvents() != null && newCompilationDto.getEvents().size() != 0) {
            // получаем список id событий в этой подборке
            Set<Long> eventIdList = newCompilationDto.getEvents();

            // выгружаем все события
            Set<Event> events = eventRepository.findAllByIdIn(eventIdList);
            Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

            // сохраняем все события (сами объекты) в подборке и сохраняем в репозитории
            compilation.setEvents(events);
            compilationRepository.save(compilation);
            return compilationMapper.toCompilationDto(compilation);
        }

        // новая подборка без событий
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        if (compilation.getEvents() == null) {
            compilation.setEvents(new HashSet<>());
        }
        compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(compilation);
    }


    // удаление подборки
    @Override
    public void deleteCompilation(Long compilationId) {
        compilationRepository.deleteById(compilationId);
    }

    // обновить информацию о подборке
    @Override
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        // выгружаем подборку из БД
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("The compilation doesn't exist"));
        // если в присланной подборке есть события, то сохраняем их в выгруженной подборке
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            Set<Long> eventIdList = updateCompilationRequest.getEvents();
            Set<Event> events = eventRepository.findAllByIdIn(eventIdList);
            compilation.setEvents(events);
        }
        // обновляем закреплено на главной странице или нет
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        // обновляем название/заголовок
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        // сохраняем в БД
        compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(compilation);
    }
}