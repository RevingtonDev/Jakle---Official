import { Stomp } from "@stomp/stompjs";
import React, { Component } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Outlet, useNavigate, useParams } from "react-router-dom";
import SockJS from "sockjs-client";
import { get_info, get_messages } from "../api/request";
import { ENDPOINTS } from "../api/routes";
import { execute } from "../store";

import { Navbar } from "./Navbar";

export const Layout = (props) => {
  return (
    <LayoutComponent
      {...props}
      navigate={useNavigate()}
      dispatch={useDispatch()}
      params={useParams()}
      storeData={useSelector((state) => state.reducer)}
    />
  );
};

class LayoutComponent extends Component {
  constructor(props) {
    super(props);
    this.socket = Stomp.over(new SockJS("/api/connect"));

    //this.props.dispatch(sendToServer({ type: "set", payload: sendToServer }));
  }

  async componentDidMount() {
    this.socket.onUnhandledMessage = async (e) => {
      const { action, data } = JSON.parse(e.body);
      if (action === "add_friend") {
        const res = await get_info(data);
        if (res) {
          if (action)
            this.props.dispatch(
              execute({ type: action, payload: res.results })
            );
        }
      } else {
        if (action)
          this.props.dispatch(execute({ type: action, payload: data }));
      }

      if (
        action &&
        action === "delete_friend" &&
        data === this.props.storeData.id
      )
        this.props.navigate("/op/dashboard");
    };
    this.socket.onWebSocketError = (evt) => {
      this.props.dispatch(execute({ type: "set_connection", payload: false }));
    }
    this.socket.onWebSocketClose = (evt) => {
      this.props.dispatch(execute({ type: "set_connection", payload: false }));
    }
    this.socket.connect({}, () => {
      this.socket.subscribe(ENDPOINTS.SUBSCRIBE);
      this.props.dispatch(execute({ type: "set_connection", payload: true }));
    });

    const messages = await get_messages();
    if (messages && messages.code === 200) {
      messages.results.forEach((elm) => {
        this.props.dispatch(execute({ type: "message_recieve", payload: elm }));
      });
    }
  }

  componentDidUpdate() {
    this.sendAndClearStore();
  }

  componentWillUnmount() {
    if (this.socket.connected) {
      this.socket.unsubscribe(ENDPOINTS.SUBSCRIBE);
      this.socket.close();
    }
  }

  sendAndClearStore() {
    const { toSend } = this.props.storeData;
    if (this.socket.connected && toSend && toSend.length > 0) {
      toSend.forEach((elm) => {
        this.socket.send(elm.dest, {}, JSON.stringify(elm.data));
      });
      this.props.dispatch(execute({ type: "clear_send" }));
    }
  }

  render() {
    return (
      <>
        {!this.props.storeData.connected && (
          <>
            <div
              className="window-fill center"
              style={{
                backgroundColor: "rgba(200,200,200,0.2)",
                position: "fixed",
                top: "0",
                backdropFilter: "blur(12px)",
                zIndex: "5000",
              }}
            >
              <div
                className="dummy center capsule"
                style={{
                  width: "200px",
                  height: "25px",
                  padding: "20px 25px",
                  fontFamily: "lato",
                  backgroundClip: "text",
                  MozBackgroundClip: "text",
                  WebkitBackgroundClip: "text",
                  border: "3px solid silver",
                  fontSize: "1.3rem",
                  fontWeight: "700",
                  color: "transparent",
                  boxShadow: "0 0 8px black",
                }}
              >
                Connecting...
              </div>
            </div>
          </>
        )}
        <Navbar />
        <Outlet />
      </>
    );
  }
}
