import { configureStore, combineReducers } from "@reduxjs/toolkit";
import { ENDPOINTS } from "./api/routes";

const defState = {
  data: [],
  toSend: [],
  messages: {},
  id: 0,
  connected: false,
  hasNewNotifications: false,
};

const reducer = (state = defState, { type, payload }) => {
  console.log(type, payload);
  switch (type) {
    case "update_users":
      return {
        ...state,
        data: payload,
      };
    case "make_online":
      const data = state.data.map((elm) => {
        if (elm.id === payload) {
          elm.activity = 1;
          elm.time = undefined;
        }

        return elm;
      });
      return { ...state, data: data };
    case "make_offline":
      return {
        ...state,
        data: state.data.map((elm) => {
          if (elm.id === payload.id) {
            elm.activity = 0;
            elm.time = payload.time;
          }

          return elm;
        }),
      };
    case "make_typing":
      return {
        ...state,
        data: state.data.map((elm) => {
          if (elm.id === payload.id) {
            elm.typing = payload.activity;
          }

          return elm;
        }),
      };
    case "recieve":
      return {
        ...state,
        data: state.data.map((elm) => {
          if (elm.id === payload && elm.id !== state.id)
            elm.count = !elm.count ? 1 : elm.count + 1;
          else if (elm.id === payload.id && payload.data === "clear")
            elm.count = undefined;
          return elm;
        }),
      };
    case "clear_send":
      return {
        ...state,
        toSend: [],
      };
    case "send":
      state.toSend.push(payload);
      if (payload.data.action === "active" && payload.data.content === "")
        state.data.forEach((elm) => (elm.typing = false));
      return { ...state };
    case "message_send":
      if (!state.messages[payload.content.id])
        state.messages[payload.content.id] = [];

      state.messages[payload.content.id].push({
        sent: true,
        content: payload.content.content,
      });
      state.toSend.push({ dest: ENDPOINTS.MESSAGE, data: payload });
      return { ...state };
    case "message_recieve":
      if (!state.messages[payload.id]) state.messages[payload.id] = [];
      state.messages[payload.id].push({
        sent: payload.send,
        content: payload.content,
      });

      return { ...state };
    case "set_id":
      return { ...state, id: payload };
    case "set_connection":
      return { ...state, connected: payload };
    case "delete_friend":
      return {
        ...state,
        data: state.data.filter((elm) => elm.id !== payload),
      };
    case "add_friend":
      state.data.push(payload);

      return { ...state };
    case "notify":
      return { ...state, hasNewNotifications: payload };
    default:
      return { ...state };
  }
};

const execute = (payload) => {
  return payload;
};

const rootReducer = combineReducers({ reducer });

export const store = configureStore({ reducer: rootReducer });

export { execute };
