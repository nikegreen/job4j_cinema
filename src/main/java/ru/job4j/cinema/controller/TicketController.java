package ru.job4j.cinema.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.job4j.cinema.model.Session;
import ru.job4j.cinema.model.Ticket;
import ru.job4j.cinema.model.User;
import ru.job4j.cinema.service.RoomService;
import ru.job4j.cinema.service.SeatService;
import ru.job4j.cinema.service.TicketService;
import ru.job4j.cinema.util.ModelSet;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

/**
 * <p>TicketController class. Покупка билета</p>
 * @author nikez
 * @version $Id: $Id
 */
@ThreadSafe
@Controller
public class TicketController {
    private final TicketService ticketService;
    private final RoomService roomService;
    private final SeatService seatService;

    public TicketController(
            TicketService ticketService,
            RoomService roomService,
            SeatService seatService) {
        this.ticketService = ticketService;
        this.roomService = roomService;
        this.seatService = seatService;
    }

    /**
     * <p>Окно покупки билета</p>
     * @param model - объект передаёт данные в страницу
     * @param httpSession - объект http сессии
     * @param cellIndex - место в ряду
     * @return - тип {@link java.lang.String} содержит:
     *  "buy" - OK
     */
    @GetMapping("/formBuy/{cellIndex}")
    public String formBuy(
            Model model,
            HttpSession httpSession,
            @PathVariable("cellIndex") int cellIndex) {
        model = ModelSet.fromSession(model, httpSession);
        Session cinemaSession = (Session) httpSession.getAttribute("cinemasession");
        model.addAttribute("seats",
                roomService.getSeats(
                        cinemaSession.getId(),
                        cinemaSession.getRoom().getId(),
                        seatService,
                        ticketService
                )
        );
        httpSession.setAttribute("cell_index", cellIndex);
        return "buy";
    }

    /**
     * <p>Обработка покупки билета</p>
     * @param httpSession - объект http сессии
     * @return - тип {@link java.lang.String} содержит:
     *  "redirect:/index" - билет не куплен
     *  "redirect:/formBuyFail" - место занято или ошибка БД;
     *  "redirect:/formTickets" - билет куплен;
     */
    @PostMapping("/createTicket")
    public String createTicket(HttpSession httpSession) {
        Session cinemaSession = (Session) httpSession.getAttribute("cinemasession");
        Integer row = (Integer) httpSession.getAttribute("row_index");
        Integer cell = (Integer) httpSession.getAttribute("cell_index");
        User user = (User) httpSession.getAttribute("user");
        if ((cinemaSession == null) || (row == null) || (cell == null) || (user == null)) {
            return "redirect:/index";
        }
        Ticket ticket = new Ticket(
                0,
                cinemaSession,
                row.intValue(),
                cell.intValue(),
                user
        );
        Optional<Ticket> res = ticketService.add(ticket);
        if (res.isPresent()) {
            httpSession.setAttribute("session_filtr", cinemaSession.getId());
            return "redirect:/formTickets";
        }
        return "redirect:/formBuyFail";
    }

    /**
     * <p>Форма с купленными билетами для пользователя (из http сессии)</p>
     * @param model - объект передаёт данные в страницу
     * @param httpSession - объект http сессии
     * @return - тип {@link java.lang.String} содержит: "tickets";
     */
    @GetMapping("/formTickets")
    public String formTickets(Model model, HttpSession httpSession) {
        model = ModelSet.fromSession(model, httpSession);
        Session cinemaSession = (Session) httpSession.getAttribute("cinemasession");
        User user = (User) httpSession.getAttribute("user");
        int sessionFiltr = Optional.ofNullable(
                (Integer) httpSession.getAttribute("session_filtr"))
                .orElse(0)
                .intValue();
        List<Ticket> tickets;
        if (sessionFiltr == 0) {
            tickets = ticketService.findAllByUser(user.getId());
        } else {
            tickets = ticketService.findAllBySessionAndUser(cinemaSession.getId(), user.getId());
        }
        model.addAttribute("tickets", tickets);
        return "tickets";
    }

    /**
     * <p>Окно сообщающая об неудачной покупке билета</p>
     * @param model - объект передаёт данные в страницу
     * @param session - объект http сессии
     * @return - тип {@link java.lang.String} содержит: "tickets";
     * @return
     */
    @GetMapping("/formBuyFail")
    public String formBuyFail(Model model, HttpSession session) {
        model = ModelSet.fromSession(model, session);
        return "buyFail";
    }
}
