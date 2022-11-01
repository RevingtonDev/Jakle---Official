import React, { Component } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import { Layout } from "./components/Layout";
import { Home } from "./pages/Home";
import { Error } from "./pages/Error";

import "./styles/_global.css";
import "./styles/component.css";
import "./styles/app.css";
import { Dashboard } from "./pages/Dashboard";
import { Chat } from "./components/Chat";
import { Provider } from "react-redux";
import { store } from "./store";
import { Logo } from "./components/Images";
import { Discover } from "./pages/Discover";
import { Notifications } from "./pages/Notifications";
import { Settings } from "./pages/Settings";

export class App extends Component {
  render() {
    return (
      <>
        <Provider store={store}>
          <Router>
            <Routes>
              <Route path="" index element={<Home />} />
              <Route path="op" element={<Layout />}>
                <Route
                  index
                  element={<Navigate to={"./dashboard"} replace={true} />}
                />
                <Route path="dashboard" element={<Dashboard />}>
                  <Route
                    index
                    element={
                      <>
                        <div className="parent-fill center">
                          <img
                            style={{ height: "80%" }}
                            src={Logo}
                            alt="logo"
                          />
                        </div>
                      </>
                    }
                  />
                  <Route path=":id" element={<Chat />} />
                </Route>
                {/*<Route path="profile" element={<Profile />} />*/}
                <Route path="notifications" element={<Notifications />} />
                <Route path="discover" element={<Discover />} />
                <Route path="settings" element={<Settings />} />
                <Route path="*" element={<Error />} />
              </Route>
            </Routes>
          </Router>
        </Provider>
      </>
    );
  }
}
